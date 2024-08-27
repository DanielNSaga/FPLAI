import React, { Component } from 'react';
import axios from 'axios';

/**
 * A component that allows users to search for players and select one from the results.
 * It includes an input field with a dropdown that displays matching players as the user types.
 */
class SearchableBox extends Component {
    constructor(props) {
        super(props);
        this.state = {
            searchQuery: '',
            searchResults: [],
            isDropdownOpen: false,
        };

        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleSearch = this.handleSearch.bind(this);
        this.handleSelectPlayer = this.handleSelectPlayer.bind(this);
        this.handleClickOutside = this.handleClickOutside.bind(this);
        this.inputRef = React.createRef();
        this.dropdownRef = React.createRef();
    }

    componentDidMount() {
        // Add an event listener to detect clicks outside the component
        document.addEventListener('mousedown', this.handleClickOutside);
    }

    componentWillUnmount() {
        // Remove the event listener when the component is unmounted
        document.removeEventListener('mousedown', this.handleClickOutside);
    }

    componentDidUpdate(prevProps) {
        // Clear the search when the selected player is removed
        if (prevProps.player !== this.props.player && this.props.player === null) {
            this.setState({ searchQuery: '', searchResults: [], isDropdownOpen: false });
        }
    }

    handleInputChange(event) {
        const searchQuery = event.target.value;
        this.setState({ searchQuery }, () => {
            // Trigger the search when the query is at least 2 characters long
            if (searchQuery.length >= 2) {
                this.handleSearch();
            } else {
                this.setState({ searchResults: [], isDropdownOpen: false });
            }
        });
    }

    handleSearch() {
        const { searchQuery } = this.state;
        const { elementType } = this.props;

        axios.get('https://fplai.onrender.com/api/players/search', {
            params: {
                elementType,
                keyword: searchQuery,
            },
        })
            .then(response => {
                const results = Array.isArray(response.data) ? response.data : [];
                this.setState({
                    searchResults: results,
                    isDropdownOpen: true,
                });
            })
            .catch(error => {
                this.setState({ searchResults: [], isDropdownOpen: true });
            });
    }

    handleSelectPlayer(player) {
        // Handle the selection of a player from the dropdown
        this.setState({
            isDropdownOpen: false,
            searchQuery: '', // Clear the search field when a player is selected
        });

        if (this.props.onAssignPlayer) {
            this.props.onAssignPlayer(player);
        }
    }

    handleClickOutside(event) {
        // Close the dropdown if a click is detected outside the component
        if (
            this.inputRef.current && !this.inputRef.current.contains(event.target) &&
            this.dropdownRef.current && !this.dropdownRef.current.contains(event.target)
        ) {
            this.setState({ isDropdownOpen: false });
        }
    }

    render() {
        const { searchQuery, searchResults, isDropdownOpen } = this.state;
        const { player, positionLabel } = this.props;

        return (
            <div className="relative flex flex-col h-full items-center justify-center" ref={this.inputRef}>
                <div className="text-xs text-white font-bold mb-1 text-center">
                    {positionLabel}
                </div>
                {player && (
                    <div className="text-xs text-white mb-1 text-center">
                        {player.web_name}
                    </div>
                )}
                <input
                    type="text"
                    value={searchQuery}
                    onChange={this.handleInputChange}
                    className="border border-gray-400 p-1 w-full text-black text-xs"
                    style={{
                        borderRadius: '16px',
                        backgroundColor: '#FFFFFF',
                    }}
                    placeholder="Search player..."
                />
                {isDropdownOpen && (
                    <ul
                        ref={this.dropdownRef}
                        className="absolute z-10 bg-white border border-gray-400 rounded-lg w-full mt-1 max-h-60 overflow-y-auto text-xs"
                        style={{ top: '100%' }}
                    >
                        {searchResults.length > 0 ? (
                            searchResults.map(player => (
                                <li
                                    key={player.id}
                                    onClick={() => this.handleSelectPlayer(player)}
                                    className="p-1 cursor-pointer hover:bg-gray-200 text-black"
                                >
                                    {player.web_name}
                                </li>
                            ))
                        ) : (
                            <li className="p-1 text-gray-500">No results found</li>
                        )}
                    </ul>
                )}
            </div>
        );
    }
}

export default SearchableBox;

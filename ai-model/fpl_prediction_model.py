import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor, StackingRegressor
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score
from xgboost import XGBRegressor
from catboost import CatBoostRegressor
import lightgbm as lgb
from flask import Flask, request, jsonify
import joblib

app = Flask(__name__)

# Function to load the dataset from the specified filename
def load_data(filename='fpl_player_stats.csv'):
    df = pd.read_csv(filename)
    df = df.sort_values(by=['name', 'season', 'gameweek'])
    return df

# Function to calculate the expanding average for a given column
def calculate_expanding_average(df, column):
    result = df.groupby(['name', 'season'])[column].expanding().mean().reset_index(level=[0, 1], drop=True)
    result.index = df.index
    return result

# Function to add calculated columns for various statistics
def add_calculated_columns(df):
    df['avg_points'] = calculate_expanding_average(df, 'points')
    df['avg_assists'] = calculate_expanding_average(df, 'assists')
    df['avg_goals_scored'] = calculate_expanding_average(df, 'goals_scored')
    df['avg_bonus'] = calculate_expanding_average(df, 'bonus')
    df['avg_minutes_played'] = calculate_expanding_average(df, 'minutes_played')
    df['avg_bps'] = calculate_expanding_average(df, 'bps')
    df['avg_saves'] = calculate_expanding_average(df, 'saves')
    df['avg_goals_conceded'] = calculate_expanding_average(df, 'goals_conceded')
    df['avg_penalties_saved'] = calculate_expanding_average(df, 'penalties_saved')
    df['avg_clean_sheets'] = calculate_expanding_average(df, 'clean_sheets')
    return df

# Function to merge standings data with the main DataFrame
def merge_standings_data(df, standings_df):
    standings_opponent = standings_df.rename(columns={'team': 'opponent', 'position': 'opponent_position'})
    standings_team = standings_df.rename(columns={'team': 'team', 'position': 'team_position'})

    df = df.merge(standings_opponent[['season', 'opponent', 'opponent_position']], on=['season', 'opponent'], how='left')
    df['opponent_difficulty'] = np.select(
        [(df['opponent_position'] >= 15), (df['opponent_position'] >= 7) & (df['opponent_position'] <= 14), (df['opponent_position'] < 7)],
        [1, 2, 3], default=0)

    df = df.merge(standings_team[['season', 'team', 'team_position']], on=['season', 'team'], how='left')
    df['team_ranking'] = np.select(
        [(df['team_position'] >= 15), (df['team_position'] >= 7) & (df['team_position'] <= 14), (df['team_position'] < 7)],
        [1, 2, 3], default=0)

    df['result_mapped'] = df['result'].map({'W': 3, 'D': 2, 'L': 1})
    df['form_last_5'] = df.groupby(['name', 'season'])['result_mapped'].shift(1).rolling(window=5).mean().fillna(0)

    return df

# Function to train a model of the specified type
def train_model(X, y, model_type="rf", params=None):
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    if model_type == "rf":
        model = RandomForestRegressor(random_state=42, **(params if params else {}))
    elif model_type == "lgbm":
        model = lgb.LGBMRegressor(**(params if params else {}))
    elif model_type == "stacking":
        base_models = [
            ('xgb', XGBRegressor(n_estimators=100, learning_rate=0.1)),
            ('rf', RandomForestRegressor(n_estimators=100)),
            ('catboost', CatBoostRegressor(iterations=100, learning_rate=0.1, depth=6, verbose=0))
        ]
        meta_model = LinearRegression()
        model = StackingRegressor(estimators=base_models, final_estimator=meta_model)
    else:
        raise ValueError("Invalid model_type provided")

    model.fit(X_train, y_train)
    return model, X_test, y_test

# Function to evaluate a trained model
def evaluate_model(model, X_test, y_test):
    y_pred_test = model.predict(X_test)
    test_mae = mean_absolute_error(y_test, y_pred_test)
    test_mse = mean_squared_error(y_test, y_pred_test)
    test_rmse = np.sqrt(test_mse)
    test_r2 = r2_score(y_test, y_pred_test)

    return test_mae, test_mse, test_rmse, test_r2

# Function to train and save models for each position
def train_and_save_models(df):
    positions = ['GK', 'DEF', 'MID', 'FWD']
    models = {}

    for position in positions:
        position_df = df[df['position'] == position]

        if position == 'GK':
            features = ['avg_points', 'avg_bonus', 'avg_minutes_played', 'avg_bps', 'avg_saves',
                        'avg_goals_conceded', 'avg_penalties_saved', 'avg_clean_sheets',
                        'opponent_difficulty', 'team_ranking',
                        'influence', 'selected', 'transfers_balance',
                        'transfers_in', 'transfers_out', 'value', 'was_home', 'form_last_5']
        elif position == 'DEF':
            features = ['avg_points', 'avg_bonus', 'avg_minutes_played', 'avg_bps', 'avg_clean_sheets',
                        'avg_goals_conceded', 'opponent_difficulty', 'team_ranking',
                        'influence', 'selected', 'transfers_balance',
                        'transfers_in', 'transfers_out', 'value', 'was_home', 'form_last_5']
        elif position == 'MID':
            features = ['avg_points', 'avg_assists', 'avg_goals_scored', 'avg_bonus', 'avg_minutes_played',
                        'avg_bps', 'opponent_difficulty', 'team_ranking', 'creativity',
                        'ict_index', 'influence', 'selected', 'threat', 'transfers_balance', 'transfers_in',
                        'transfers_out', 'value', 'was_home', 'form_last_5']
        elif position == 'FWD':
            features = ['avg_points', 'avg_assists', 'avg_goals_scored', 'avg_bonus', 'avg_minutes_played',
                        'avg_bps', 'opponent_difficulty', 'team_ranking', 'creativity',
                        'ict_index', 'influence', 'selected', 'threat', 'transfers_balance', 'transfers_in',
                        'transfers_out', 'value', 'was_home', 'form_last_5']

        # Logging to verify that columns exist
        missing_cols = [col for col in features if col not in position_df.columns]
        if missing_cols:
            raise ValueError(f"Missing columns for {position}: {missing_cols}")

        X = position_df[features]
        y = position_df['points']

        model, _, _ = train_model(X, y, model_type="stacking")
        models[position] = model
        joblib.dump(model, f'model_{position.lower()}.pkl')

    return models

# Load models (assuming they are already trained and saved)
def load_models():
    models = {
        'GK': joblib.load('model_gk.pkl'),
        'DEF': joblib.load('model_def.pkl'),
        'MID': joblib.load('model_mid.pkl'),
        'FWD': joblib.load('model_fwd.pkl')
    }
    return models

# Flask API to handle predictions
@app.route('/predict', methods=['POST'])
def predict():
    try:
        # Log the incoming request
        data = request.get_json()
        app.logger.info("Received data: %s", data)

        # Check if data is empty or None
        if not data:
            app.logger.error("No data provided in the request.")
            return jsonify({"error": "No data provided"}), 400

        # Extract positions and features separately
        try:
            positions = [player['position'] for player in data]
            features = [player['features'] for player in data]
        except KeyError as e:
            app.logger.error("Missing key in the input data: %s", e)
            return jsonify({"error": f"Missing key: {str(e)}"}), 400

        # Log extracted positions and features
        app.logger.info("Extracted positions: %s", positions)
        app.logger.info("Extracted features: %s", features)

        # Create DataFrame from features
        df = pd.DataFrame(features)
        app.logger.info("Created DataFrame:\n%s", df.head())

        # Add position to DataFrame
        df['position'] = positions
        app.logger.info("Updated DataFrame with positions:\n%s", df.head())

        # Load pre-trained models
        models = load_models()
        app.logger.info("Loaded models for positions: %s", list(models.keys()))

        # Ensure all positions are the same, as predictions are done per position
        if len(set(df['position'])) != 1:
            app.logger.error("All players in a single request must have the same position.")
            return jsonify({"error": "All players in a single request must have the same position"}), 400

        position = df['position'].iloc[0]
        app.logger.info("Position determined for prediction: %s", position)

        # Define features based on position
        if position == 'GK':
            feature_columns = ['avg_points', 'avg_bonus', 'avg_minutes_played', 'avg_bps', 'avg_saves',
                               'avg_goals_conceded', 'avg_penalties_saved', 'avg_clean_sheets',
                               'opponent_difficulty', 'team_ranking',
                               'influence', 'selected', 'transfers_balance',
                               'transfers_in', 'transfers_out', 'value', 'was_home', 'form_last_5']
        elif position == 'DEF':
            feature_columns = ['avg_points', 'avg_bonus', 'avg_minutes_played', 'avg_bps', 'avg_clean_sheets',
                               'avg_goals_conceded', 'opponent_difficulty', 'team_ranking',
                               'influence', 'selected', 'transfers_balance',
                               'transfers_in', 'transfers_out', 'value', 'was_home', 'form_last_5']
        elif position == 'MID':
            feature_columns = ['avg_points', 'avg_assists', 'avg_goals_scored', 'avg_bonus', 'avg_minutes_played',
                               'avg_bps', 'opponent_difficulty', 'team_ranking', 'creativity',
                               'ict_index', 'influence', 'selected', 'threat', 'transfers_balance', 'transfers_in',
                               'transfers_out', 'value', 'was_home', 'form_last_5']
        elif position == 'FWD':
            feature_columns = ['avg_points', 'avg_assists', 'avg_goals_scored', 'avg_bonus', 'avg_minutes_played',
                               'avg_bps', 'opponent_difficulty', 'team_ranking', 'creativity',
                               'ict_index', 'influence', 'selected', 'threat', 'transfers_balance', 'transfers_in',
                               'transfers_out', 'value', 'was_home', 'form_last_5']
        else:
            app.logger.error("Invalid position provided: %s", position)
            return jsonify({"error": "Invalid position"}), 400

        # Check if all required columns are present
        missing_cols = [col for col in feature_columns if col not in df.columns]
        if missing_cols:
            app.logger.error("Missing columns for position %s: %s", position, missing_cols)
            return jsonify({"error": f"Missing columns for {position}: {missing_cols}"}), 400

        # Predict using the model for the given position
        model = models.get(position)
        app.logger.info("Using model for position %s to predict.", position)
        predictions = model.predict(df[feature_columns])
        app.logger.info("Predictions: %s", predictions)

        return jsonify(predictions.tolist())

    except Exception as e:
        app.logger.error("An error occurred during prediction: %s", str(e))
        return jsonify({"error": str(e)}), 500

# Endpoint to trigger model training
@app.route('/train', methods=['POST'])
def train_models():
    # Load and process data
    df = load_data('fpl_player_stats.csv')
    df = add_calculated_columns(df)

    # Merge standings data
    standings_df = get_standings_data()
    df = merge_standings_data(df, standings_df)

    # Train and save models
    models = train_and_save_models(df)

    return jsonify({"message": "Models trained and saved successfully"}), 200

# Function to generate standings data (example)
def get_standings_data():
    standings_data = {
        'season': ['2016/17']*20 + ['2017/18']*20 + ['2018/19']*20 + ['2019/20']*20 + ['2020/21']*20 + ['2021/22']*20 + ['2022/23']*20,
        'team': ['Chelsea', 'Tottenham', 'Manchester City', 'Liverpool', 'Arsenal', 'Manchester United', 'Everton', 'Southampton', 'Bournemouth', 'West Bromwich Albion',
                 'West Ham United', 'Leicester City', 'Stoke City', 'Crystal Palace', 'Swansea City', 'Burnley', 'Watford', 'Hull City', 'Middlesbrough', 'Sunderland',
                 'Manchester City', 'Manchester United', 'Tottenham', 'Liverpool', 'Chelsea', 'Arsenal', 'Burnley', 'Everton', 'Leicester City', 'Newcastle United',
                 'Crystal Palace', 'Bournemouth', 'West Ham United', 'Watford', 'Brighton & Hove Albion', 'Huddersfield Town', 'Southampton', 'Swansea City', 'Stoke City', 'West Bromwich Albion',
                 'Manchester City', 'Liverpool', 'Chelsea', 'Tottenham', 'Arsenal', 'Manchester United', 'Wolverhampton Wanderers', 'Everton', 'Leicester City', 'West Ham United',
                 'Watford', 'Crystal Palace', 'Newcastle United', 'Bournemouth', 'Burnley', 'Southampton', 'Brighton & Hove Albion', 'Cardiff City', 'Fulham', 'Huddersfield Town',
                 'Liverpool', 'Manchester City', 'Manchester United', 'Chelsea', 'Leicester City', 'Tottenham', 'Wolverhampton Wanderers', 'Arsenal', 'Sheffield United', 'Burnley',
                 'Southampton', 'Everton', 'Newcastle United', 'Crystal Palace', 'Brighton & Hove Albion', 'West Ham United', 'Aston Villa', 'Bournemouth', 'Watford', 'Norwich City',
                 'Manchester City', 'Manchester United', 'Liverpool', 'Chelsea', 'Leicester City', 'West Ham United', 'Tottenham', 'Arsenal', 'Leeds United', 'Everton',
                 'Aston Villa', 'Newcastle United', 'Wolverhampton Wanderers', 'Crystal Palace', 'Southampton', 'Brighton & Hove Albion', 'Burnley', 'Fulham', 'West Bromwich Albion', 'Sheffield United',
                 'Manchester City', 'Liverpool', 'Chelsea', 'Tottenham', 'Arsenal', 'Manchester United', 'West Ham United', 'Leicester City', 'Brighton & Hove Albion', 'Wolverhampton Wanderers',
                 'Newcastle United', 'Crystal Palace', 'Brentford', 'Aston Villa', 'Southampton', 'Everton', 'Leeds United', 'Burnley', 'Watford', 'Norwich City',
                 'Manchester City', 'Arsenal', 'Manchester United', 'Newcastle United', 'Liverpool', 'Brighton & Hove Albion', 'Aston Villa', 'Tottenham', 'Brentford', 'Fulham',
                 'Crystal Palace', 'Chelsea', 'Wolverhampton Wanderers', 'West Ham United', 'Bournemouth', 'Nottingham Forest', 'Everton', 'Leicester City', 'Leeds United', 'Southampton'],
        'position': [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                     1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                     1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                     1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                     1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                     1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                     1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]
    }
    standings_df = pd.DataFrame(standings_data)
    return standings_df

if __name__ == '__main__':
    app.run(port=5000)

import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor

# This script trains a RandomForestRegressor model to predict Fantasy Premier League player points.
# It involves loading data, defining features and the target variable, splitting the data into training
# and test sets, and fitting the model with optimized hyperparameters.


# Load the data
# The file contains training data for the Fantasy Premier League AI model
df = pd.read_csv('/Users/danielsaga/IdeaProjects/FPLAI/ai-model/fpl_training 2.csv', delimiter=';')

# Define features (X) and target variable (y)
# Features include all columns except 'label', which is our target variable
X = df.drop(['label'], axis=1)
y = df['label']

# Split the dataset into training and test sets
# 80% of the data is used for training and 20% for testing
# The random_state parameter ensures reproducibility
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=19)

# Train the RandomForestRegressor model with the best hyperparameters
# These hyperparameters were optimized previously to improve model performance
best_params = {
    'bootstrap': False,
    'max_depth': 30,
    'max_features': 'sqrt',
    'min_samples_leaf': 4,
    'min_samples_split': 2,
    'n_estimators': 300
}

rfr = RandomForestRegressor(
    random_state=13,
    n_estimators=best_params['n_estimators'],
    max_features=best_params['max_features'],
    max_depth=best_params['max_depth'],
    min_samples_split=best_params['min_samples_split'],
    min_samples_leaf=best_params['min_samples_leaf'],
    bootstrap=best_params['bootstrap']
)

# Fit the model to the training data
rfr.fit(X_train, y_train)



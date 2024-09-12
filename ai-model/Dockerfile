# Use an official Python image as base
FROM python:3.9-slim

# Set the working directory in the container
WORKDIR /app

# Copy the requirements file into the container
COPY requirements.txt .

# Install dependencies from requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of the application files to the working directory
COPY . .

# Expose the port that the Flask application will run on
EXPOSE 8000

# Run the application with Gunicorn as the production server
CMD ["gunicorn", "--bind", "0.0.0.0:8000", "fpl_prediction_model:app"]

import requests
from flask import Flask, jsonify

app = Flask(__name__)

# Replace with your actual financial API endpoint
EXCHANGE_RATE_API_URL = "https://api.exchangerate-api.com/v4/latest/USD"


@app.route("/getUSDEUR", methods=["GET"])
def get_usd_eur():
    try:
        response = requests.get(EXCHANGE_RATE_API_URL)
        data = response.json()
        usd_to_eur = data["rates"]["EUR"]
        return jsonify({"USD_EUR": usd_to_eur})
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route("/getEURUSD", methods=["GET"])
def get_eur_usd():
    try:
        response = requests.get(EXCHANGE_RATE_API_URL)
        data = response.json()
        eur_to_usd = 1 / data["rates"]["EUR"]
        return jsonify({"EUR_USD": eur_to_usd})
    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    app.run(debug=True)

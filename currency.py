import requests
from flask import Flask, jsonify, request

app = Flask(__name__)

EXCHANGE_RATE_API_URL = "https://api.exchangerate-api.com/v4/latest/"


@app.route("/convert", methods=["GET"])
def convert_currency():
    source_currency = request.args.get("from")
    target_currency = request.args.get("to")

    if not source_currency or not target_currency:
        return jsonify(
            {"error": "Please provide both 'from' and 'to' currency parameters."}
        ), 400

    try:
        response = requests.get(EXCHANGE_RATE_API_URL + source_currency)
        data = response.json()

        if "error" in data:
            return jsonify({"error": data["error"]}), 400

        exchange_rate = data["rates"].get(target_currency)

        if exchange_rate is None:
            return jsonify({"error": f"Currency '{target_currency}' not found."}), 404

        return jsonify({f"{source_currency}-{target_currency}": exchange_rate})

    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    app.run(debug=True)

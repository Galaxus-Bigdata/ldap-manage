from flask import Flask, request
import requests

app = Flask(__name__)

@app.route('/get-rm-state', methods=['POST'])
def get_rm_state():
    # Get the hostname from the POST request
    hostname = request.form.get('hostname')

    if not hostname:
        return 'Hostname not provided', 400

    # Construct the Resource Manager API URL
    rm_api_url = f'http://{hostname}:8088/ws/v1/cluster/info'

    try:
        # Send a GET request to the Resource Manager API
        response = requests.get(rm_api_url)
        response.raise_for_status()

        # Parse the response JSON
        cluster_info = response.json()

        # Check if the Resource Manager is Active or Standby
        rm_state = cluster_info['clusterInfo']['haState']

        return f'Resource Manager state: {rm_state}', 200

    except requests.exceptions.RequestException as e:
        return f'Error: {e}', 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

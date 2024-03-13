from flask import Flask, request
import requests
from requests_kerberos import HTTPKerberosAuth, DISABLED
import os

app = Flask(__name__)

@app.route('/get-rm-state', methods=['POST'])
def get_rm_state():
    # Get the hostname from the POST request
    hostname = request.form.get('hostname')

    if not hostname:
        return 'Hostname not provided', 400

    # Construct the Resource Manager API URL
    rm_api_url = f'https://{hostname}:8088/ws/v1/cluster/info'

    try:
        # Set up Kerberos authentication
        kinit_path = '/path/to/kinit'  # Replace with the actual path to kinit
        os.environ['KRB5_CLIENT_KINITSYNC'] = 'TRUE'
        os.system(f"{kinit_path} -kt /path/to/keytab username@REALM.COM")

        # Set up SSL configuration
        ssl_cert = ('/path/to/client.crt', '/path/to/client.key')  # Replace with your SSL cert and key paths
        ssl_ca_cert = '/path/to/ca.pem'  # Replace with the path to your CA certificate

        # Send a GET request to the Resource Manager API with Kerberos and SSL
        response = requests.get(rm_api_url, auth=HTTPKerberosAuth(mutual_authentication=DISABLED), cert=ssl_cert, verify=ssl_ca_cert)
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

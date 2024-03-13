const fetch = require('node-fetch');
const https = require('https-browserify');
const fs = require('fs');
const path = require('path');

// Set up Kerberos authentication
const kerberos = require('kerberos');
const kOptions = {
  host: 'your.server.com',
  principal: 'your_principal@REALM.COM',
  keytab: fs.readFileSync('path/to/your_keytab.keytab'), // Specify the keytab file
  krb5conf: path.resolve('path/to/krb5.conf') // Specify the krb5.conf file
};

kerberos.initializeClient(kOptions, (err) => {
  if (err) {
    console.error('Kerberos initialization failed:', err);
    return;
  }

  // Load the root CA certificate
  const rootCa = fs.readFileSync('path/to/rootca.pem');

  // Create an HTTPS agent with the root CA certificate
  const agent = new https.Agent({
    ca: rootCa,
    rejectUnauthorized: true // Verify the SSL certificate
  });

  // Make the REST API call
  const apiUrl = 'https://your.api.endpoint/path';
  const options = {
    method: 'GET',
    headers: {
      'Authorization': 'Negotiate ' + kerberos.getClientString()
    },
    agent: agent
  };

  fetch(apiUrl, options)
    .then(res => res.json())
    .then(data => {
      console.log('API response:', data);
    })
    .catch(err => {
      console.error('API call failed:', err);
    });
});

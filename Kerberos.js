const fetch = require('node-fetch');
const krb5 = require('node-krb5');

// Define the URL you want to request
const url = 'https://example.com/api/data';

// Kerberos configuration
const config = {
  principal: 'HTTP/example.com@EXAMPLE.COM', // Service principal
  krb5_conf: '/path/to/krb5.conf', // Path to krb5.conf file
};

// Get the Kerberos token
krb5.getToken(config, (err, token) => {
  if (err) {
    console.error('Error getting Kerberos token:', err);
    return;
  }

  // Generate the auth header with the token
  const options = {
    method: 'GET',
    headers: {
      Authorization: `Negotiate ${token}`,
    },
  };

  // Make the request with fetch
  fetch(url, options)
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    })
    .then((data) => {
      console.log('Response data:', data);
    })
    .catch((error) => {
      console.error('Error:', error);
    });
});

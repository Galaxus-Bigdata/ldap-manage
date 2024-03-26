// Create a new iframe element
var iframe = document.createElement('iframe');

// Set attributes for the iframe
iframe.src = 'https://www.example.com';
iframe.width = '600';
iframe.height = '400';
iframe.frameBorder = '0';
iframe.allowFullscreen = true;

// Get the container element where you want to append the iframe
var container = document.getElementById('iframeContainer');

// Append the iframe to the container
container.appendChild(iframe);

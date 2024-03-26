// Define the Grafana URL
var grafanaDashboardURL = 'https://your-grafana-url.com/dashboard/db/your-dashboard-name';

// Update the Application Object
module.exports = {
  name: 'Ambari Web',
  rootElement: '#wrapper',

  // Other properties and methods...

  quickLinks: [
    // Existing quick links...

    // Add Grafana Quick Link
    {
      label: 'Grafana Dashboard',
      url: grafanaDashboardURL,
      icon: 'icon-grafana' // You can add an icon class if needed
    }
  ]
};

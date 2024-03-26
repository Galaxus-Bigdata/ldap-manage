// Define the Grafana URL
var grafanaDashboardURL = 'https://your-grafana-url.com/dashboard/db/your-dashboard-name';

// Update the Application Object
module.exports = {
  name: 'Ambari Web',
  rootElement: '#wrapper',

  // Other properties and methods...

  initQuickLinks: function() {
    // Add Grafana Quick Link using jQuery
    var $grafanaLink = $('<a>')
      .attr('href', grafanaDashboardURL)
      .text('Grafana Dashboard')
      .addClass('grafana-link');

    $('#quick-links-container').append($grafanaLink);
  }
};

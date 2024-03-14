const options = {
  url: 'http://example.com/api/data',
  method: 'GET',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_ACCESS_TOKEN'
  }
};

backendSrv.datasourceRequest(options)
  .then(response => {
    console.log('Data source response:', response.data);
  })
  .catch(error => {
    console.error('Error fetching data:', error);
  });

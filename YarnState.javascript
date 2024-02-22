const rmUrl = "http://your-resourcemanager-host:8088/ws/v1/cluster/info";

fetch(rmUrl)
  .then(response => {
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    return response.json();
  })
  .then(data => {
    // Assuming the HA state is under the "haState" field in the JSON response
    const haState = data.clusterInfo.haState;
    console.log("HA State:", haState);
    // Do something with the HA state
  })
  .catch(error => {
    console.error('There was a problem with the fetch operation:', error);
  });

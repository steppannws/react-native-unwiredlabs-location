var http = require('http');
var requestListener = function (req, res) {
  // res.writeHead(200);


	console.log("TEST")

  // res.on('data', function (chunk) {
  // 	console.log(req.data)
  // });

  res.end('Test LocationApi\n');
}

var server = http.createServer(requestListener);
server.listen(8080);
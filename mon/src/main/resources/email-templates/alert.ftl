<?xml version="1.0" encoding="UTF-8"?>

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Alert, ${alert.fqName}</title>
        <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css"></link>
        <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap-theme.min.css"></link>
	</head>
	<body>
		<h3>${alert.fqName}</h3>
		<p>${alert.message}</p>
		<pre>${alert.log!"No log."}</pre>
	</body>
</html>

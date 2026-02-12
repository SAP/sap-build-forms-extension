<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <link rel="icon" type="image/x-icon" href="./assets/${favicon}"/>
    <title>Forms Value Help Frontend</title>
    <script type="text/javascript">
        const ROUTER_BASE_NAME = "${contextPath}"
    </script>
    <script type="module" crossorigin src="./assets/${indexJS}"></script>
    <#if hasToken>
    <script type="text/javascript">
        sessionStorage.setItem("accessToken", "${accessToken}");
        sessionStorage.setItem("refreshToken", "${refreshToken}");
    </script>
    </#if>
</head>

<body style="border: 0; margin: 0; padding: 0">
<div id="root"></div>
</body>
</html>

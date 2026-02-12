<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <link rel="icon" type="image/x-icon" href="./assets/${favicon}"/>
    <title>Forms Frontend</title>
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
    <style>
        .loader {
            width: 80px;
            aspect-ratio: 1.154;
            --_g: no-repeat radial-gradient(farthest-side,#0064d9 90%,#0000);
            background:
                var(--_g) 50%  0,
                var(--_g) 0    100%,
                var(--_g) 100% 100%;
            background-size: 35% calc(35%*1.154);
            animation: l16 1s infinite;
        }
        @keyframes l16{
            50%,100% {background-position: 100% 100%,50% 0,0 100%}
        }
    </style>
</head>

<body style="border: 0; margin: 0; padding: 0">
    <div id="root" style="background-color: #f5f6f7; width: 100vw; height: 100vh;">
        <div id="splash" class="loader" style="position: relative; left: calc((100vw - 80px)/2); top: calc((100vh - 80px)/2)"></div>
    </div>
</body>
</html>

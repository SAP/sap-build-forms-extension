# Libraries

- UI5 react components

- ViteJS

- Typescript

- react-jss -> allows to override formatting of shadow-css parts, e.g. (in this case the root part

	```javascript
	const useStyles = createUseStyles({
		dateLabel: {
			"&::part(root)": {
				borderWidth: "0px !important",
				...
			}
		}
	})
	```
	
- zustand -> alternative to redux and redux-toolkit

- immer -> allows creating "allways" a new state version with zustand
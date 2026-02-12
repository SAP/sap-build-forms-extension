interface Card2Props extends React.PropsWithChildren {
    className?: string
    style?: React.CSSProperties
}

/**
 *
 * @param props
 * @returns
 */
export function Card2(props: Card2Props) {
    const { children, className, style } = props

    return (
        <div
            className={`${className}`}
            style={{
                ...style,
                borderRadius: ".5rem",
                backgroundColor: "var(--sapGroup_TitleBackground)",
            }}
        >
            <div style={{ padding: ".5rem" }}>{children}</div>
        </div>
    )
}

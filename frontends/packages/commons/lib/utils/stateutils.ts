/**
 *  Make a copy of the given array and replace an element with the same id as the replacement.
 *  If no element with the same id is found, the replacement is added to the end
 *  
 * @param currents 
 * @param replacement 
 * @returns 
 */
export function copyAndReplace<Type>(currents: Array<Type>, replacement: Type): Array<Type> {
    let found = false
    let news: Type[] = []
    for (const value of currents) {
        if ((value as any).id === (replacement as any).id) {
            news.push(replacement)
            found = true
        } else {
            news.push(value)
        }
    }
    if (!found) {
        news.push(replacement)
    }

    return news
}

/**
 *  Make a copy of the given array and delete an element with the given id.
 * 
 * @param currents 
 * @param id 
 * @returns 
 */
export function copyAndDelete<Type>(currents: Array<Type>, id: string): Array<Type> {
    let news: Type[] = []
    for (const value of currents) {
        if ((value as any).id === id) {
            continue
        }
        news.push(value)
    }

    return news
}

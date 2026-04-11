import {JSX} from "react"

import {
    FlexBox,
    FlexBoxDirection,
    Form,
    FormItem,
    Input,
    InputDomRef,
    Label,
    MessageBoxType,
    MultiComboBox,
    MultiComboBoxItem,
    MultiInput,
    Option,
    RadioButton,
    Select,
    Tab,
    Text,
    Token,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"
import {createUseStyles} from "react-jss"

import InputType from "@ui5/webcomponents/dist/types/InputType"
import {ValueHelpDef} from "../../features/model"

/**
 * Styles of the component.
 */
const useStyles = createUseStyles({
    formTextBox: {
        paddingBlock: 6,
        wordBreak: "break-all",
    },
    formText: {
        marginLeft: "2px",
    },
    formInput: {
        width: "100%",
    },
})

/**
 *  Properties of the ConfigTab component.
 */
interface ConfigTabProps {
    edit: boolean
    availableLanguages: string[]
    currentValueHelpDef: ValueHelpDef | undefined

    setCurrentValueHelpDef(v: ValueHelpDef): void

    changeLanguages(v: ValueHelpDef): void

    openMessageBox(mBoxType: MessageBoxType, mBoxText: JSX.Element, mBoxId: string): void
}

/**
 *  Configuration tab of the ValueHelp editor.
 *
 * @param props
 * @returns
 */
export default function ({
                             availableLanguages,
                             changeLanguages,
                             currentValueHelpDef,
                             edit,
                             setCurrentValueHelpDef,
                         }: ConfigTabProps) {
    const classes = useStyles()

    return (
        <Tab icon="settings" selected text="Config">
            <Form
                layout="S1 M1 L1 XL1"
                labelSpan="S4 M3 L2 XL2"
                style={{
                    alignItems: "center",
                }}
            >
                {!edit && (
                    <>
                        <FormItem labelContent={<Label>Description</Label>}>
                            <div className={classes.formTextBox}>
                                <Text className={classes.formText}>
                                    {currentValueHelpDef?.description}
                                </Text>
                            </div>
                        </FormItem>
                        <FormItem labelContent={<Label>TTL</Label>}>
                            <div className={classes.formTextBox}>
                                {currentValueHelpDef?.ttl == -1 && (
                                    <Text className={classes.formText}>static</Text>
                                )}
                                {currentValueHelpDef?.ttl == 0 && (
                                    <Text className={classes.formText}>always refresh</Text>
                                )}
                                {currentValueHelpDef && currentValueHelpDef?.ttl > 0 && (
                                    <Text className={classes.formText}>
                                        {currentValueHelpDef?.ttl} min
                                    </Text>
                                )}
                            </div>
                        </FormItem>
                        <FormItem labelContent={<Label>Languages</Label>}>
                            <div className={classes.formTextBox}>
                                <Text className={classes.formText}>
                                    {currentValueHelpDef?.languages.join(", ")}
                                </Text>
                            </div>
                        </FormItem>
                        <FormItem labelContent={<Label>Type</Label>}>
                            <div className={classes.formTextBox}>
                                <Text className={classes.formText}>
                                    {currentValueHelpDef?.type}
                                </Text>
                            </div>
                        </FormItem>
                        <FormItem labelContent={<Label>Key-Key</Label>}>
                            <div className={classes.formTextBox}>
                                <Text className={classes.formText}>
                                    {currentValueHelpDef?.keyKey}
                                </Text>
                            </div>
                        </FormItem>
                        <FormItem labelContent={<Label>Value-Keys</Label>}>
                            <div className={classes.formTextBox}>
                                <Text className={classes.formText}>
                                    {currentValueHelpDef?.valueKeys?.join(", ")}
                                </Text>
                            </div>
                        </FormItem>
                        <FormItem labelContent={<Label>Format Template</Label>}>
                            <div className={classes.formTextBox}>
                                <Text className={classes.formText}>
                                    {currentValueHelpDef?.formatTemplate}
                                </Text>
                            </div>
                        </FormItem>
                        <FormItem labelContent={<Label>Adapter</Label>}>
                            <div className={classes.formTextBox}>
                                <Text className={classes.formText}>
                                    {currentValueHelpDef?.adapter}
                                </Text>
                            </div>
                        </FormItem>
                        <FormItem labelContent={<Label>Config</Label>}>
                            <div className={classes.formTextBox}>
                                <Text className={classes.formText}>
                                    {currentValueHelpDef?.config}
                                </Text>
                            </div>
                        </FormItem>
                    </>
                )}
                {edit && currentValueHelpDef && (
                    <>
                        <FormItem labelContent={<Label>Description</Label>}>
                            <Input
                                type={InputType.Text}
                                value={currentValueHelpDef?.description}
                                placeholder={currentValueHelpDef?.description}
                                className={classes.formInput}
                                onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                    setCurrentValueHelpDef({
                                        ...currentValueHelpDef!,
                                        description:
                                            e.target.attributes.getNamedItem("value")!.nodeValue!,
                                    })
                                }}
                            />
                        </FormItem>
                        <FormItem labelContent={<Label>TTL</Label>}>
                            <FlexBox direction={FlexBoxDirection.Column}>
                                <RadioButton
                                    text="static"
                                    checked={currentValueHelpDef?.ttl == -1}
                                    onChange={() => {
                                        setCurrentValueHelpDef({
                                            ...currentValueHelpDef!,
                                            ttl: -1,
                                        })
                                    }}
                                />
                                <RadioButton
                                    text="always refresh"
                                    checked={currentValueHelpDef?.ttl == 0}
                                    onChange={() => {
                                        setCurrentValueHelpDef({
                                            ...currentValueHelpDef!,
                                            ttl: 0,
                                        })
                                    }}
                                />
                                <FlexBox>
                                    <RadioButton
                                        text="time buffer (>0) (in min)"
                                        checked={
                                            currentValueHelpDef?.ttl != -1 &&
                                            currentValueHelpDef?.ttl != 0
                                        }
                                        onChange={() => {
                                            setCurrentValueHelpDef({
                                                ...currentValueHelpDef!,
                                                ttl: 1,
                                            })
                                        }}
                                    />
                                    {currentValueHelpDef?.ttl != -1 &&
                                        currentValueHelpDef?.ttl != 0 && (
                                            <Input
                                                value={currentValueHelpDef?.ttl.toString()}
                                                style={{width: 87}}
                                                type={InputType.Number}
                                                valueState={
                                                    currentValueHelpDef?.ttl > 0
                                                        ? "None"
                                                        : "Negative"
                                                }
                                                valueStateMessage={
                                                    <span>Time buffer must be greater than 0</span>
                                                }
                                                onChange={(
                                                    e: Ui5CustomEvent<InputDomRef, never>,
                                                ) => {
                                                    setCurrentValueHelpDef({
                                                        ...currentValueHelpDef!,
                                                        ttl: Number(
                                                            e.target.attributes.getNamedItem(
                                                                "value",
                                                            )!.nodeValue!,
                                                        ),
                                                    })
                                                }}
                                            />
                                        )}
                                </FlexBox>
                            </FlexBox>
                        </FormItem>
                        <FormItem labelContent={<Label>Languages</Label>}>
                            <MultiComboBox
                                onSelectionChange={(e) => {
                                    const newValueHelpDef = {
                                        ...currentValueHelpDef!,
                                        languages: e.detail.items.map((i) => i.id),
                                    }
                                    setCurrentValueHelpDef(newValueHelpDef)
                                    changeLanguages(newValueHelpDef)
                                }}
                            >
                                {availableLanguages.map((l) => {
                                    return (
                                        <MultiComboBoxItem
                                            text={l}
                                            id={l}
                                            key={l}
                                            selected={currentValueHelpDef?.languages.includes(l)}
                                        />
                                    )
                                })}
                            </MultiComboBox>
                        </FormItem>
                        <FormItem labelContent={<Label>Type</Label>}>
                            <Select
                                onChange={(e) => {
                                    setCurrentValueHelpDef({
                                        ...currentValueHelpDef!,
                                        type: e.detail.selectedOption.id,
                                    })
                                }}
                            >
                                <Option
                                    id="freestyle"
                                    selected={currentValueHelpDef.type === "freestyle"}
                                >
                                    Freestyle
                                </Option>
                                <Option
                                    id="currency"
                                    selected={currentValueHelpDef.type === "currency"}
                                >
                                    Currency
                                </Option>
                            </Select>
                        </FormItem>
                        <FormItem labelContent={<Label>Key-Key</Label>}>
                            <Input
                                type={InputType.Text}
                                value={currentValueHelpDef?.keyKey}
                                placeholder={currentValueHelpDef?.keyKey}
                                className={classes.formInput}
                                onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                    setCurrentValueHelpDef({
                                        ...currentValueHelpDef!,
                                        keyKey: e.target.attributes.getNamedItem("value")!
                                            .nodeValue!,
                                    })
                                }}
                            />
                        </FormItem>
                        <FormItem labelContent={<Label>Value-Keys</Label>}>
                            <MultiInput
                                className={classes.formInput}
                                tokens={currentValueHelpDef.valueKeys?.map((item) => (
                                    <Token key={item} text={item}></Token>
                                ))}
                                onChange={(e) => {
                                    const v = currentValueHelpDef!
                                    v.valueKeys = v.valueKeys ?? []
                                    v.valueKeys.push(
                                        e.target.attributes.getNamedItem("value")!.nodeValue!,
                                    )
                                    setCurrentValueHelpDef({...v})
                                    // clear input
                                    e.target.attributes.getNamedItem("value")!.nodeValue = ""
                                }}
                                onTokenDelete={(e) => {
                                    let valueKeys = currentValueHelpDef.valueKeys
                                    if (valueKeys) {
                                        e.detail.tokens.forEach((token) => {
                                            valueKeys = valueKeys!.filter((v) => v !== token.text)
                                        })
                                        setCurrentValueHelpDef({
                                            ...currentValueHelpDef,
                                            valueKeys,
                                        })
                                    }
                                }}
                            />
                        </FormItem>
                        <FormItem labelContent={<Label>Format Template</Label>}>
                            <Input
                                type={InputType.Text}
                                value={currentValueHelpDef?.formatTemplate}
                                placeholder={currentValueHelpDef?.formatTemplate}
                                className={classes.formInput}
                                onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                    setCurrentValueHelpDef({
                                        ...currentValueHelpDef!,
                                        formatTemplate:
                                            e.target.attributes.getNamedItem("value")!.nodeValue!,
                                    })
                                }}
                            />
                        </FormItem>
                        <FormItem labelContent={<Label>Adapter: </Label>}>
                            <Input
                                type={InputType.Text}
                                value={currentValueHelpDef?.adapter}
                                placeholder={currentValueHelpDef?.adapter}
                                valueState={
                                    currentValueHelpDef?.adapter.trim().length <= 0
                                        ? "Negative"
                                        : "None"
                                }
                                valueStateMessage={<span>Adapter must not be empty</span>}
                                className={classes.formInput}
                                onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                    setCurrentValueHelpDef({
                                        ...currentValueHelpDef!,
                                        adapter:
                                            e.target.attributes.getNamedItem("value")!.nodeValue!,
                                    })
                                }}
                            />
                        </FormItem>
                        <FormItem labelContent={<Label>Config</Label>}>
                            <Input
                                type={InputType.Text}
                                value={currentValueHelpDef?.config}
                                placeholder={currentValueHelpDef?.config}
                                className={classes.formInput}
                                onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                    setCurrentValueHelpDef({
                                        ...currentValueHelpDef!,
                                        config: e.target.attributes.getNamedItem("value")!
                                            .nodeValue!,
                                    })
                                }}
                            />
                        </FormItem>
                    </>
                )}
            </Form>
        </Tab>
    )
}

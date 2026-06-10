import { JSX } from "react"

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
    RadioButton,
    Tab,
    Text,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"
import { createUseStyles } from "react-jss"

import InputType from "@ui5/webcomponents/dist/types/InputType"
import { ValueHelpDef } from "../../features/model"

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
        width: "80%",
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
export default function (props: ConfigTabProps) {
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
                {!props.edit && (
                    <>
                        <FormItem labelContent={<Label>Description</Label>}>
                            <div className={classes.formTextBox}>
                                <Text className={classes.formText}>
                                    {props.currentValueHelpDef?.description}
                                </Text>
                            </div>
                        </FormItem>

                        <FormItem labelContent={<Label>TTL</Label>}>
                            <div className={classes.formTextBox}>
                                {props.currentValueHelpDef?.ttl == -1 && (
                                    <Text className={classes.formText}>static</Text>
                                )}
                                {props.currentValueHelpDef?.ttl == 0 && (
                                    <Text className={classes.formText}>refresh</Text>
                                )}
                                {props.currentValueHelpDef &&
                                    props.currentValueHelpDef?.ttl > 0 && (
                                        <Text className={classes.formText}>
                                            {props.currentValueHelpDef?.ttl} min
                                        </Text>
                                    )}
                            </div>
                        </FormItem>

                        <FormItem labelContent={<Label>Adapter</Label>}>
                            <div className={classes.formTextBox}>
                                <Text className={classes.formText}>
                                    {props.currentValueHelpDef?.adapter}
                                </Text>
                            </div>
                        </FormItem>

                        <FormItem labelContent={<Label>Config</Label>}>
                            <div className={classes.formTextBox}>
                                <Text className={classes.formText}>
                                    {props.currentValueHelpDef?.config}
                                </Text>
                            </div>
                        </FormItem>

                        <FormItem labelContent={<Label>Languages</Label>}>
                            <div className={classes.formTextBox}>
                                <Text className={classes.formText}>
                                    {props.currentValueHelpDef?.languages.join(", ")}
                                </Text>
                            </div>
                        </FormItem>
                    </>
                )}
                {props.edit && props.currentValueHelpDef && (
                    <>
                        <FormItem labelContent={<Label>Description</Label>}>
                            <Input
                                type={InputType.Text}
                                value={props.currentValueHelpDef?.description}
                                placeholder={props.currentValueHelpDef?.description}
                                className={classes.formInput}
                                onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                    props.setCurrentValueHelpDef({
                                        ...props.currentValueHelpDef!,
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
                                    checked={props.currentValueHelpDef?.ttl == -1}
                                    onChange={() => {
                                        props.setCurrentValueHelpDef({
                                            ...props.currentValueHelpDef!,
                                            ttl: -1,
                                        })
                                    }}
                                />
                                <RadioButton
                                    text="refresh"
                                    checked={props.currentValueHelpDef?.ttl == 0}
                                    onChange={() => {
                                        props.setCurrentValueHelpDef({
                                            ...props.currentValueHelpDef!,
                                            ttl: 0,
                                        })
                                    }}
                                />
                                <FlexBox>
                                    <RadioButton
                                        text="time buffer (>0) (in min)"
                                        checked={
                                            props.currentValueHelpDef?.ttl != -1 &&
                                            props.currentValueHelpDef?.ttl != 0
                                        }
                                        onChange={() => {
                                            props.setCurrentValueHelpDef({
                                                ...props.currentValueHelpDef!,
                                                ttl: 1,
                                            })
                                        }}
                                    />
                                    {props.currentValueHelpDef?.ttl != -1 &&
                                        props.currentValueHelpDef?.ttl != 0 && (
                                            <Input
                                                value={props.currentValueHelpDef?.ttl.toString()}
                                                style={{ width: 87 }}
                                                type={InputType.Number}
                                                valueState={
                                                    props.currentValueHelpDef?.ttl > 0
                                                        ? "None"
                                                        : "Negative"
                                                }
                                                valueStateMessage={
                                                    <span>Time buffer must be greater than 0</span>
                                                }
                                                onChange={(
                                                    e: Ui5CustomEvent<InputDomRef, never>,
                                                ) => {
                                                    props.setCurrentValueHelpDef({
                                                        ...props.currentValueHelpDef!,
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
                        <FormItem labelContent={<Label>Adapter: </Label>}>
                            <Input
                                type={InputType.Text}
                                value={props.currentValueHelpDef?.adapter}
                                placeholder={props.currentValueHelpDef?.adapter}
                                valueState={
                                    props.currentValueHelpDef?.adapter.trim().length <= 0
                                        ? "Negative"
                                        : "None"
                                }
                                valueStateMessage={<span>Adapter must not be empty</span>}
                                className={classes.formInput}
                                onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                    props.setCurrentValueHelpDef({
                                        ...props.currentValueHelpDef!,
                                        adapter:
                                            e.target.attributes.getNamedItem("value")!.nodeValue!,
                                    })
                                }}
                            />
                        </FormItem>
                        <FormItem labelContent={<Label>Config</Label>}>
                            <Input
                                type={InputType.Text}
                                value={props.currentValueHelpDef?.config}
                                placeholder={props.currentValueHelpDef?.config}
                                className={classes.formInput}
                                onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                    props.setCurrentValueHelpDef({
                                        ...props.currentValueHelpDef!,
                                        config: e.target.attributes.getNamedItem("value")!
                                            .nodeValue!,
                                    })
                                }}
                            />
                        </FormItem>
                        <FormItem labelContent={<Label>Languages</Label>}>
                            <MultiComboBox
                                onSelectionChange={function Xs(e) {
                                    var newValueHelpDef = {
                                        ...props.currentValueHelpDef!,
                                        languages: e.detail.items.map((i) => i.id),
                                    }
                                    props.setCurrentValueHelpDef(newValueHelpDef)
                                    props.changeLanguages(newValueHelpDef)
                                }}
                            >
                                {props.availableLanguages.map((l) => {
                                    return (
                                        <MultiComboBoxItem
                                            text={l}
                                            id={l}
                                            key={l}
                                            selected={props.currentValueHelpDef?.languages.includes(
                                                l,
                                            )}
                                        />
                                    )
                                })}
                            </MultiComboBox>
                        </FormItem>
                    </>
                )}
            </Form>
        </Tab>
    )
}

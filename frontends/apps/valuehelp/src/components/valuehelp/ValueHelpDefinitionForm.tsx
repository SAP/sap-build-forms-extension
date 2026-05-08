import { RefObject } from "react"

import { createUseStyles } from "react-jss"
import {
    FlexBox,
    FlexBoxDirection,
    Form,
    FormItem,
    Input,
    InputDomRef,
    Label,
    MultiComboBox,
    MultiComboBoxItem,
    MultiInput,
    Option,
    RadioButton,
    Select,
    Text,
    Token,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"
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
        width: "100%",
    },
})

/**
 * Properties of the ValueHelpDefinitionForm component.
 */
interface ValueHelpDefinitionFormProps {
    edit: boolean
    availableLanguages: string[]
    refValueHelpDef: RefObject<ValueHelpDef | undefined>
    changeLanguages(v: ValueHelpDef): void
}

/**
 *
 * @returns
 */
export default function ({
    edit,
    availableLanguages,
    refValueHelpDef,
    changeLanguages,
}: ValueHelpDefinitionFormProps) {
    const classes = useStyles()

    return (
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
                                {refValueHelpDef.current?.description}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem labelContent={<Label>TTL</Label>}>
                        <div className={classes.formTextBox}>
                            {refValueHelpDef.current?.ttl == -1 && (
                                <Text className={classes.formText}>static</Text>
                            )}
                            {refValueHelpDef.current?.ttl == 0 && (
                                <Text className={classes.formText}>always refresh</Text>
                            )}
                            {refValueHelpDef.current && refValueHelpDef.current?.ttl > 0 && (
                                <Text className={classes.formText}>
                                    {refValueHelpDef.current?.ttl} min
                                </Text>
                            )}
                        </div>
                    </FormItem>
                    <FormItem labelContent={<Label>Languages</Label>}>
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {refValueHelpDef.current?.languages.join(", ")}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem labelContent={<Label>Type</Label>}>
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {refValueHelpDef.current?.type}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem labelContent={<Label>Key-Key</Label>}>
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {refValueHelpDef.current?.keyKey}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem labelContent={<Label>Value-Keys</Label>}>
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {refValueHelpDef.current?.valueKeys?.join(", ")}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem labelContent={<Label>Format Template</Label>}>
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {refValueHelpDef.current?.formatTemplate}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem labelContent={<Label>Adapter</Label>}>
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {refValueHelpDef.current?.adapter}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem labelContent={<Label>Config</Label>}>
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {refValueHelpDef.current?.config}
                            </Text>
                        </div>
                    </FormItem>
                </>
            )}
            {edit && refValueHelpDef.current && (
                <>
                    <FormItem labelContent={<Label>Description</Label>}>
                        <Input
                            type={InputType.Text}
                            value={refValueHelpDef.current?.description}
                            placeholder={refValueHelpDef.current?.description}
                            className={classes.formInput}
                            onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                refValueHelpDef.current = {
                                    ...refValueHelpDef.current!,
                                    description:
                                        e.target.attributes.getNamedItem("value")!.nodeValue!,
                                }
                            }}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>TTL</Label>}>
                        <FlexBox direction={FlexBoxDirection.Column}>
                            <RadioButton
                                text="static"
                                checked={refValueHelpDef.current?.ttl == -1}
                                onChange={() => {
                                    refValueHelpDef.current = {
                                        ...refValueHelpDef.current!,
                                        ttl: -1,
                                    }
                                }}
                            />
                            <RadioButton
                                text="always refresh"
                                checked={refValueHelpDef.current?.ttl == 0}
                                onChange={() => {
                                    refValueHelpDef.current = {
                                        ...refValueHelpDef.current!,
                                        ttl: 0,
                                    }
                                }}
                            />
                            <FlexBox>
                                <RadioButton
                                    text="time buffer (>0) (in min)"
                                    checked={
                                        refValueHelpDef.current?.ttl != -1 &&
                                        refValueHelpDef.current?.ttl != 0
                                    }
                                    onChange={() => {
                                        refValueHelpDef.current = {
                                            ...refValueHelpDef.current!,
                                            ttl: 1,
                                        }
                                    }}
                                />
                                {refValueHelpDef.current?.ttl != -1 &&
                                    refValueHelpDef.current?.ttl != 0 && (
                                        <Input
                                            value={refValueHelpDef.current?.ttl.toString()}
                                            style={{ width: 87 }}
                                            type={InputType.Number}
                                            valueState={
                                                refValueHelpDef.current?.ttl > 0
                                                    ? "None"
                                                    : "Negative"
                                            }
                                            valueStateMessage={
                                                <span>Time buffer must be greater than 0</span>
                                            }
                                            onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                                refValueHelpDef.current = {
                                                    ...refValueHelpDef.current!,
                                                    ttl: Number(
                                                        e.target.attributes.getNamedItem("value")!
                                                            .nodeValue!,
                                                    ),
                                                }
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
                                    ...refValueHelpDef.current!,
                                    languages: e.detail.items.map((i) => i.id),
                                }
                                refValueHelpDef.current = newValueHelpDef
                                changeLanguages(newValueHelpDef)
                            }}
                        >
                            {availableLanguages.map((l) => {
                                return (
                                    <MultiComboBoxItem
                                        text={l}
                                        id={l}
                                        key={l}
                                        selected={refValueHelpDef.current?.languages.includes(l)}
                                    />
                                )
                            })}
                        </MultiComboBox>
                    </FormItem>
                    <FormItem labelContent={<Label>Type</Label>}>
                        <Select
                            onChange={(e) => {
                                refValueHelpDef.current = {
                                    ...refValueHelpDef.current!,
                                    type: e.detail.selectedOption.id,
                                }
                            }}
                        >
                            <Option
                                id="freestyle"
                                selected={refValueHelpDef.current?.type === "freestyle"}
                            >
                                Freestyle
                            </Option>
                            <Option
                                id="currency"
                                selected={refValueHelpDef.current?.type === "currency"}
                            >
                                Currency
                            </Option>
                        </Select>
                    </FormItem>
                    <FormItem labelContent={<Label>Key-Key</Label>}>
                        <Input
                            type={InputType.Text}
                            value={refValueHelpDef.current?.keyKey}
                            placeholder={refValueHelpDef.current?.keyKey}
                            className={classes.formInput}
                            onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                refValueHelpDef.current = {
                                    ...refValueHelpDef.current!,
                                    keyKey: e.target.attributes.getNamedItem("value")!.nodeValue!,
                                }
                            }}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>Value-Keys</Label>}>
                        <MultiInput
                            className={classes.formInput}
                            tokens={refValueHelpDef.current?.valueKeys?.map((item) => (
                                <Token key={item} text={item}></Token>
                            ))}
                            onChange={(e) => {
                                const v = refValueHelpDef.current!
                                v.valueKeys = v.valueKeys ?? []
                                v.valueKeys.push(
                                    e.target.attributes.getNamedItem("value")!.nodeValue!,
                                )
                                refValueHelpDef.current = { ...v }
                                // clear input
                                e.target.attributes.getNamedItem("value")!.nodeValue = ""
                            }}
                            onTokenDelete={(e) => {
                                let valueKeys = refValueHelpDef.current?.valueKeys
                                if (valueKeys) {
                                    e.detail.tokens.forEach((token) => {
                                        valueKeys = valueKeys!.filter((v) => v !== token.text)
                                    })
                                    refValueHelpDef.current = {
                                        ...refValueHelpDef.current!,
                                        valueKeys,
                                    }
                                }
                            }}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>Format Template</Label>}>
                        <Input
                            type={InputType.Text}
                            value={refValueHelpDef.current?.formatTemplate}
                            placeholder={refValueHelpDef.current?.formatTemplate}
                            className={classes.formInput}
                            onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                refValueHelpDef.current = {
                                    ...refValueHelpDef.current!,
                                    formatTemplate:
                                        e.target.attributes.getNamedItem("value")!.nodeValue!,
                                }
                            }}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>Adapter: </Label>}>
                        <Input
                            type={InputType.Text}
                            value={refValueHelpDef.current?.adapter}
                            placeholder={refValueHelpDef.current?.adapter}
                            valueState={
                                refValueHelpDef.current?.adapter.trim().length <= 0
                                    ? "Negative"
                                    : "None"
                            }
                            valueStateMessage={<span>Adapter must not be empty</span>}
                            className={classes.formInput}
                            onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                refValueHelpDef.current = {
                                    ...refValueHelpDef.current!,
                                    adapter: e.target.attributes.getNamedItem("value")!.nodeValue!,
                                }
                            }}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>Config</Label>}>
                        <Input
                            type={InputType.Text}
                            value={refValueHelpDef.current?.config}
                            placeholder={refValueHelpDef.current?.config}
                            className={classes.formInput}
                            onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                refValueHelpDef.current = {
                                    ...refValueHelpDef.current!,
                                    config: e.target.attributes.getNamedItem("value")!.nodeValue!,
                                }
                            }}
                        />
                    </FormItem>
                </>
            )}
        </Form>
    )
}

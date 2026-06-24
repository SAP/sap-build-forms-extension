import { Controller, UseFormReturn } from "react-hook-form"
import { createUseStyles } from "react-jss"
import {
    FlexBox,
    FlexBoxDirection,
    Form,
    FormItem,
    Input,
    Label,
    MultiComboBox,
    MultiComboBoxItem,
    MultiInput,
    Option,
    RadioButton,
    Select,
    Text,
    Token,
} from "@ui5/webcomponents-react"
import InputType from "@ui5/webcomponents/dist/types/InputType"

import { ValueHelpDef } from "../../features/model"
import { requiredValueState, Severity, useMessages, valueState } from "commons"

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
    isNew: boolean
    editMode: boolean
    availableLanguages: string[]
    availableAdapters: string[]
    changeLanguages(v: ValueHelpDef): void
    form: UseFormReturn<ValueHelpDef>
}

/**
 * Form for displaying and editing the details of a value help definition.
 *
 * @param props - The properties of the component.
 * @param props.edit - Whether the form is in edit mode or display mode.
 * @param props.isNew - Whether the value help definition is new or existing.
 * @param props.availableLanguages - The list of available languages to choose from.
 * @param props.refValueHelpDef - A reference to the value help definition being edited or displayed.
 * @param props.changeLanguages - A function to call when the languages of the value help definition are changed.
 * @returns The JSX element representing the form.
 */
export default function ({
    editMode,
    isNew,
    availableLanguages,
    availableAdapters,
    changeLanguages,
    form,
}: ValueHelpDefinitionFormProps) {
    const classes = useStyles()
    const messages = useMessages()
    const {
        control,
        formState: { errors },
        setValue,
        getValues,
        watch,
    } = form

    const isCurrency = watch("type") === "currency"

    return (
        <Form
            layout="S1 M1 L1 XL1"
            labelSpan="S4 M3 L2 XL2"
            style={{
                alignItems: "center",
            }}
        >
            {!editMode && (
                <>
                    <FormItem labelContent={<Label>Description</Label>}>
                        <Controller
                            name="description"
                            control={control}
                            render={({ field: { value } }) => (
                                <Text className={classes.formText}>{value}</Text>
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>TTL</Label>}>
                        <Controller
                            name="ttl"
                            control={control}
                            render={({ field: { value } }) => (
                                <div className={classes.formTextBox}>
                                    {value == -1 && (
                                        <Text className={classes.formText}>static</Text>
                                    )}
                                    {value == 0 && (
                                        <Text className={classes.formText}>always refresh</Text>
                                    )}
                                    {value > 0 && (
                                        <Text className={classes.formText}>{value} min</Text>
                                    )}
                                </div>
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>Languages</Label>}>
                        <Controller
                            name="languages"
                            control={control}
                            render={({ field: { value } }) => (
                                <div className={classes.formTextBox}>
                                    <Text className={classes.formText}>{value?.join(", ")}</Text>
                                </div>
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>Type</Label>}>
                        <Controller
                            name="type"
                            control={control}
                            render={({ field: { value } }) => (
                                <div className={classes.formTextBox}>
                                    <Text className={classes.formText}>{value}</Text>
                                </div>
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>Key-Key</Label>}>
                        <Controller
                            name="keyKey"
                            control={control}
                            render={({ field: { value } }) => (
                                <div className={classes.formTextBox}>
                                    <Text className={classes.formText}>{value}</Text>
                                </div>
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>Value-Keys</Label>}>
                        <Controller
                            name="valueKeys"
                            control={control}
                            render={({ field: { value } }) => (
                                <div className={classes.formTextBox}>
                                    <Text className={classes.formText}>{value?.join(", ")}</Text>
                                </div>
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>Format Template</Label>}>
                        <Controller
                            name="formatTemplate"
                            control={control}
                            render={({ field: { value } }) => (
                                <div className={classes.formTextBox}>
                                    <Text className={classes.formText}>{value}</Text>
                                </div>
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>Adapter</Label>}>
                        <Controller
                            name="adapter"
                            control={control}
                            render={({ field: { value } }) => (
                                <div className={classes.formTextBox}>
                                    <Text className={classes.formText}>{value}</Text>
                                </div>
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>Config</Label>}>
                        <Controller
                            name="config"
                            control={control}
                            render={({ field: { value } }) => (
                                <div className={classes.formTextBox}>
                                    <Text className={classes.formText}>{value}</Text>
                                </div>
                            )}
                        />
                    </FormItem>
                </>
            )}
            {editMode && (
                <>
                    {isNew && (
                        <FormItem labelContent={<Label required>Name</Label>}>
                            <Controller
                                name="id"
                                control={control}
                                rules={{ required: true }}
                                render={({ field }) => (
                                    <Input
                                        {...field}
                                        type={InputType.Text}
                                        className={classes.formInput}
                                        valueState={valueState(errors["id"])}
                                    />
                                )}
                            />
                        </FormItem>
                    )}
                    <FormItem labelContent={<Label>Description</Label>}>
                        <Controller
                            name="description"
                            control={control}
                            render={({ field }) => (
                                <Input
                                    type={InputType.Text}
                                    className={classes.formInput}
                                    {...field}
                                />
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>TTL</Label>}>
                        <Controller
                            name="ttl"
                            control={control}
                            render={({ field: { value, onChange, onBlur } }) => (
                                <FlexBox direction={FlexBoxDirection.Column}>
                                    <RadioButton
                                        text="static"
                                        checked={value == -1}
                                        onChange={(evt) => {
                                            onChange(evt)
                                            setValue("ttl", -1)
                                        }}
                                        onBlur={onBlur}
                                    />
                                    <RadioButton
                                        text="always refresh"
                                        checked={value == 0}
                                        onChange={(evt) => {
                                            onChange(evt)
                                            setValue("ttl", 0)
                                        }}
                                        onBlur={onBlur}
                                    />
                                    <FlexBox>
                                        <RadioButton
                                            text="time buffer (>0) (in min)"
                                            checked={value != -1 && value != 0}
                                            onChange={(evt) => {
                                                onChange(evt)
                                                setValue("ttl", 1)
                                            }}
                                            onBlur={onBlur}
                                        />
                                        {value != -1 && value != 0 && (
                                            <Input
                                                value={value.toString()}
                                                style={{ marginLeft: ".5em", width: "2em" }}
                                                type={InputType.Number}
                                                valueState={valueState(errors["ttl"])}
                                                valueStateMessage={
                                                    <span>Time buffer must be greater than 0</span>
                                                }
                                                onChange={onChange}
                                                onBlur={onBlur}
                                            />
                                        )}
                                    </FlexBox>
                                </FlexBox>
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label required>Languages</Label>}>
                        <Controller
                            name="languages"
                            control={control}
                            rules={{ required: true }}
                            render={({ field: { onChange, onBlur, value } }) => (
                                <MultiComboBox
                                    valueState={errors["languages"] ? "Negative" : "None"}
                                    onSelectionChange={(evt) => {
                                        const langs = evt.detail.items.map((i) => i.id)
                                        onChange(langs)
                                        changeLanguages({ ...getValues(), languages: langs })
                                    }}
                                >
                                    {[...new Set(availableLanguages)].map((l) => {
                                        return (
                                            <MultiComboBoxItem
                                                text={l}
                                                id={l}
                                                key={l}
                                                selected={value.includes(l)}
                                            />
                                        )
                                    })}
                                </MultiComboBox>
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label required>Type</Label>}>
                        <Controller
                            name="type"
                            control={control}
                            rules={{ required: true }}
                            render={({ field: { value } }) => (
                                <Select
                                    key={value}
                                    onChange={(evt) => {
                                        const t = evt.detail.selectedOption.dataset.id!
                                        setValue("type", t)
                                        if (t === "currency") {
                                            setValue("keyKey", "isocode")
                                            setValue("valueKeys", ["digits", "name", "symbol"])
                                        } else {
                                            setValue("keyKey", "")
                                            setValue("valueKeys", [])
                                        }
                                    }}
                                >
                                    <Option value="freestyle" data-id="freestyle" selected={value === "freestyle"}>
                                        Freestyle
                                    </Option>
                                    <Option value="currency" data-id="currency" selected={value === "currency"}>
                                        Currency
                                    </Option>
                                </Select>
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label required>Key-Key</Label>}>
                        <Controller
                            name="keyKey"
                            control={control}
                            rules={{
                                required: true,
                                validate: (v) => !(getValues("valueKeys") ?? []).includes(v) || "Must not overlap with Value-Keys",
                            }}
                            render={({ field }) => (
                                <Input
                                    {...field}
                                    type={InputType.Text}
                                    className={classes.formInput}
                                    readonly={isCurrency}
                                    valueState={valueState(errors["keyKey"])}
                                    valueStateMessage={<span>{errors["keyKey"]?.message ?? "Key-Key is required"}</span>}
                                />
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label required>Value-Keys</Label>}>
                        <Controller
                            name="valueKeys"
                            control={control}
                            rules={{ required: true }}
                            render={({ field: { value } }) => (
                                <MultiInput
                                    className={classes.formInput}
                                    readonly={isCurrency}
                                    tokens={value?.map((item) => (
                                        <Token key={item} text={item}></Token>
                                    ))}
                                    onChange={(evt) => {
                                        const newKey = evt.target.attributes.getNamedItem("value")?.nodeValue ?? ""
                                        const trimmed = newKey.trim()
                                        if (trimmed.length > 0) {
                                            if (value?.includes(trimmed)) {
                                                messages.toast(Severity.Warning, "err_valuekey_duplicate")
                                            } else if (trimmed === getValues("keyKey")) {
                                                messages.toast(Severity.Warning, "err_valuekey_overlaps_keykey")
                                            } else {
                                                setValue("valueKeys", [...(value ?? []), trimmed])
                                            }
                                        }
                                        // clear input
                                        evt.target.attributes.getNamedItem("value")!.nodeValue = ""
                                    }}
                                    onTokenDelete={(evt) => {
                                        const remaining = (value ?? []).filter(
                                            (v) => !evt.detail.tokens.some((t) => t.text === v)
                                        )
                                        setValue("valueKeys", remaining)
                                    }}
                                    valueState={requiredValueState(value)}
                                    valueStateMessage={<span>Value-Keys must not be empty</span>}
                                />
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>Format Template</Label>}>
                        <Controller
                            name="formatTemplate"
                            control={control}
                            render={({ field }) => (
                                <Input
                                    {...field}
                                    type={InputType.Text}
                                    className={classes.formInput}
                                />
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label required>Adapter</Label>}>
                        <Controller
                            name="adapter"
                            control={control}
                            rules={{ required: true }}
                            render={({ field: { value } }) => {
                                const options = availableAdapters.includes("local")
                                    ? availableAdapters
                                    : ["local", ...availableAdapters]
                                return (
                                    <Select
                                        className={classes.formInput}
                                        valueState={valueState(errors["adapter"])}
                                        onChange={(evt) =>
                                            setValue("adapter", evt.detail.selectedOption.dataset.id!)
                                        }
                                    >
                                        {options.map((a) => (
                                            <Option key={a} data-id={a} selected={value === a}>
                                                {a}
                                            </Option>
                                        ))}
                                    </Select>
                                )
                            }}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>Config</Label>}>
                        <Controller
                            name="config"
                            control={control}
                            render={({ field }) => (
                                <Input
                                    {...field}
                                    type={InputType.Text}
                                    className={classes.formInput}
                                />
                            )}
                        />
                    </FormItem>
                </>
            )}
        </Form>
    )
}

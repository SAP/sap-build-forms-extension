import { RefObject, useEffect } from "react"

import { useIntl } from "react-intl"
import { Controller, useForm, UseFormReturn } from "react-hook-form"

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
import { requiredValueState, valueState, valueStateMessage } from "commons"

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
    changeLanguages,
    form,
}: ValueHelpDefinitionFormProps) {
    const intl = useIntl()
    const classes = useStyles()
    const {
        control,
        formState: { errors },
        setValue,
    } = form

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
                                rules={{ required: true }}
                                control={control}
                                render={({ field }) => (
                                    <Input
                                        {...field}
                                        type={InputType.Text}
                                        valueState={valueState(errors["id"])}
                                        valueStateMessage={valueStateMessage(intl, errors["id"], {
                                            name: intl.formatMessage({
                                                id: "common_err_required",
                                            }),
                                            field: "Name",
                                        })}
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
                                    valueState={valueState(errors["description"])}
                                    valueStateMessage={valueStateMessage(
                                        intl,
                                        errors["description"],
                                        {
                                            name: intl.formatMessage({
                                                id: "common_err_required",
                                            }),
                                            field: "Description",
                                        },
                                    )}
                                />
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label required>TTL</Label>}>
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
                            rules={{ required: true, validate: (v) => v.length > 0 }}
                            render={({ field: { onChange, onBlur, value } }) => (
                                <MultiComboBox
                                    onSelectionChange={(evt) => {
                                        onChange(evt)
                                        setValue(
                                            "languages",
                                            evt.detail.items.map((i) => i.id),
                                        )
                                        // changeLanguages(value.join(","))
                                    }}
                                    valueState={valueState(errors["languages"])}
                                    valueStateMessage={valueStateMessage(
                                        intl,
                                        errors["languages"],
                                        {
                                            name: intl.formatMessage({
                                                id: "common_err_required",
                                            }),
                                            field: "Languages",
                                        },
                                    )}
                                >
                                    {availableLanguages.map((l) => {
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
                            render={({ field }) => (
                                <Select
                                    {...field}
                                    // onChange={(evt) => {
                                    //     setValue("type", evt.detail.selectedOption.dataset.id!)
                                    // }}
                                >
                                    <Option value="freestyle" data-id="freestyle">
                                        Freestyle
                                    </Option>
                                    <Option value="currency" data-id="currency">
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
                            rules={{ required: true }}
                            render={({ field }) => (
                                <Input
                                    {...field}
                                    type={InputType.Text}
                                    className={classes.formInput}
                                    valueState={valueState(errors["keyKey"])}
                                    valueStateMessage={valueStateMessage(intl, errors["keyKey"], {
                                        name: intl.formatMessage({
                                            id: "common_err_required",
                                        }),
                                        field: "Key-Key",
                                    })}
                                />
                            )}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label required>Value-Keys</Label>}>
                        <Controller
                            name="valueKeys"
                            control={control}
                            rules={{ required: true, validate: (v) => v && v.length > 0 }}
                            render={({ field: { value } }) => (
                                <MultiInput
                                    className={classes.formInput}
                                    tokens={value?.map((item) => (
                                        <Token key={item} text={item}></Token>
                                    ))}
                                    onChange={(evt) => {
                                        value = value ?? []
                                        value.push(
                                            evt.target.attributes.getNamedItem("value")!.nodeValue!,
                                        )
                                        setValue("valueKeys", value)

                                        // clear input
                                        evt.target.attributes.getNamedItem("value")!.nodeValue = ""
                                    }}
                                    onTokenDelete={(evt) => {
                                        if (value) {
                                            evt.detail.tokens.forEach((token) => {
                                                value = value!.filter((v) => v !== token.text)
                                            })
                                        }
                                    }}
                                    valueState={requiredValueState(
                                        refValueHelpDef.current?.valueKeys,
                                    )}
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
                    <FormItem labelContent={<Label required>Adapter: </Label>}>
                        <Controller
                            name="adapter"
                            control={control}
                            rules={{ required: true }}
                            render={({ field }) => (
                                <Input
                                    {...field}
                                    type={InputType.Text}
                                    className={classes.formInput}
                                    valueState={valueState(errors["adapter"])}
                                    valueStateMessage={valueStateMessage(intl, errors["adapter"], {
                                        name: intl.formatMessage({
                                            id: "common_err_required",
                                        }),
                                        field: "Adapter",
                                    })}
                                />
                            )}
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

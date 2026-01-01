import * as React from "react";
import { z } from "zod";
import { useForm, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Input } from "@/components/ui/input";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import SettingsCard from "@/features/settings/components/settings-card";
import SaveFooter from "@/features/settings/components/save-footer";

const nicknameSchema = z.object({
  nickname: z
    .string()
    .min(1, "请输入昵称")
    .max(32, "最多使用32个字符"),
});

type NicknameFormValues = z.infer<typeof nicknameSchema>;

type AccountNicknameCardProps = {
  initialValue?: string | null;
  onSave?: (nickname: string) => void;
  /** increment this prop to force-clear the input after successful save */
  resetKey?: number;
};

export function AccountNicknameCard({ initialValue = "", onSave, resetKey }: AccountNicknameCardProps) {
  const form = useForm<NicknameFormValues>({
    resolver: zodResolver(nicknameSchema),
    defaultValues: { nickname: initialValue ?? "" },
    mode: "onSubmit",
  });

  const { reset, formState } = form;
  const nickname = useWatch({ control: form.control, name: "nickname", defaultValue: initialValue ?? "" }) as string;
  const isDisabled = (nickname ?? "").trim() === "" || formState.isSubmitting;

  React.useEffect(() => {
    reset({ nickname: initialValue ?? "" });
  }, [initialValue, reset]);

  React.useEffect(() => {
    if (resetKey == null) return;
    reset({ nickname: "" });
  }, [resetKey, reset]);

  return (
    <SettingsCard
      title={<span>昵称</span>}
      description={<span>请输入您的全名，或您觉得合适的显示名称。</span>}
      footer={
        <SaveFooter
          hint={<span>请最多使用32个字符。</span>}
          disabled={isDisabled}
          onClick={form.handleSubmit((values) => onSave?.(values.nickname))}
        />
      }
      stackChildren
    >
      <Form {...form}>
        <FormField
          control={form.control}
          name="nickname"
          render={({ field }) => (
            <FormItem>
              <FormLabel className="sr-only">昵称</FormLabel>
              <FormControl>
                <Input
                  className="rounded-sm shadow-none w-72"
                  {...field}
                  maxLength={32}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
      </Form>
    </SettingsCard>
  );
}
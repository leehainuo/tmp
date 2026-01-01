import * as React from "react";
import { z } from "zod";
import { useForm, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Input } from "@/components/ui/input";
import SettingsCard from "@/features/settings/components/settings-card";
import SaveFooter from "@/features/settings/components/save-footer";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";

type AccountPasswordCardProps = {
  onChangePassword?: (newPassword: string) => void;
  /** increment to clear password inputs after successful change */
  resetKey?: number;
};

const passwordSchema = z
  .object({
    newPassword: z.string().min(8, "请至少使用8个字符"),
    confirmPassword: z.string().min(1, "请确认密码"),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: "密码不一致",
    path: ["confirmPassword"],
  });

type PasswordFormValues = z.infer<typeof passwordSchema>;

export function AccountPasswordCard({ onChangePassword, resetKey }: AccountPasswordCardProps) {
  const form = useForm<PasswordFormValues>({
    resolver: zodResolver(passwordSchema),
    defaultValues: { newPassword: "", confirmPassword: "" },
    mode: "onSubmit",
  });
  const { reset, formState } = form;
  const newPassword = useWatch({ control: form.control, name: "newPassword", defaultValue: "" }) as string;
  const isDisabled = (newPassword ?? "").trim() === "" || formState.isSubmitting;

  React.useEffect(() => {
    if (resetKey == null) return;
    reset({ newPassword: "", confirmPassword: "" });
  }, [resetKey, reset]);

  return (
    <SettingsCard
      title={<span>密码</span>}
      description={<span>请输入并确认您的新密码。</span>}
      footer={
        <SaveFooter
          hint={<span>请至少使用8个字符。</span>}
          disabled={isDisabled}
          onClick={form.handleSubmit((values) => onChangePassword?.(values.newPassword))}
        />
      }
      stackChildren
    >
      <Form {...form}>
        <FormField
          control={form.control}
          name="newPassword"
          render={({ field }) => (
            <FormItem className="mb-4">
              <FormLabel className="sr-only">新密码</FormLabel>
              <FormControl>
                <Input
                  type="password"
                  className="rounded-sm shadow-none w-72"
                  placeholder="新密码"
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="confirmPassword"
          render={({ field }) => (
            <FormItem>
              <FormLabel className="sr-only">确认新密码</FormLabel>
              <FormControl>
                <Input
                  type="password"
                  className="rounded-sm shadow-none w-72"
                  placeholder="确认新密码"
                  {...field}
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
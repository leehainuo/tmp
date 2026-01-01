import * as React from "react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";

type SaveFooterProps = {
  hint?: React.ReactNode;
  disabled?: boolean;
  onClick?: () => void;
  label?: React.ReactNode;
  className?: string;
};

export function SaveFooter({ hint, disabled, onClick, label = "保存", className }: SaveFooterProps) {
  return (
    <div className={cn("flex items-center", className)}>
      <div className="flex-1">{hint}</div>
      <div>
        {/* reuse shared Button from UI for consistent variants, sizes and accessibility */}
        <Button
          type="button"
          disabled={disabled}
          onClick={onClick}
          className={cn(
            // keep visual parity when disabled
            disabled ? "bg-muted text-muted-foreground" : undefined,
            // ensure cursor shows not-allowed when disabled (button has disabled:pointer-events-none)
            "disabled:cursor-not-allowed rounded-sm",
            // allow parent padding to affect button height by removing fixed h-* from variant
            "h-8"
          )}
        >
          {label}
        </Button>
      </div>
    </div>
  );
}

export default SaveFooter;



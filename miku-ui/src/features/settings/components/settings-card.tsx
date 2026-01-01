import * as React from "react";
import { cn } from "@/lib/utils";

type SettingsCardProps = {
  title: React.ReactNode;
  description?: React.ReactNode;
  children?: React.ReactNode;
  footer?: React.ReactNode;
  className?: string;
  /** when true, render children as a full-width row below the title/description */
  stackChildren?: boolean;
};

export function SettingsCard({
  title,
  description,
  children,
  footer,
  className,
  stackChildren,
}: SettingsCardProps) {
  return (
    <div className={cn("rounded-md border text-[0.875rem] leading-6 bg-card w-full max-w-210", className)}>
      {/* Use row layout so left title/description and right content sit side-by-side */}
      <div className="p-6">
        <div className="flex items-start justify-between">
          <div className="flex-1 pe-6">
            <h4 className="text-xl leading-8 font-semibold mb-3">{title}</h4>
            {description ? <p className="my-3">{description}</p> : null}
          </div>

          {/* If not stacking children, show them at right */}
          {!stackChildren ? <div className="flex-none">{children}</div> : null}
        </div>

        {/* If stacking children, render them full-width below */}
        {stackChildren ? <div className="mt-4">{children}</div> : null}
      </div>

      {footer ? <footer className="rounded-b-md border-t px-6 py-3 text-muted-foreground bg-inset h-14.25">{footer}</footer> : null}
    </div>
  );
}

export default SettingsCard;



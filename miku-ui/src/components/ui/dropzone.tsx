/**
 * 适用于 React 和 Next.js 应用的拖放式文件上传组件。
 * 采用 TypeScript 支持、Tailwind CSS 样式和 shadcn/ui 设计，具备文件验证、预览和可自定义的上传界面。
 */
import * as React from "react";
import { useCallback, useEffect, useRef, useState } from "react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { UploadCloud, X, ImageIcon } from "lucide-react";

type AcceptMap = Record<string, string[]>;

type DropzoneProps = {
  accept?: AcceptMap;
  maxFiles?: number;
  maxSize?: number; // bytes
  minSize?: number; // bytes
  onDrop?: (files: File[]) => void;
  onError?: (err: Error) => void;
  src?: File[] | undefined;
  multiple?: boolean;
  className?: string;
  children?: React.ReactNode;
};

type DropzoneContextType = {
  files?: File[];
  previews: string[];
  removeFile: (index: number) => void;
  clear: () => void;
};

const DropzoneContext = React.createContext<DropzoneContextType | null>(null);

export function Dropzone({
  accept,
  maxFiles = 1,
  maxSize = Infinity,
  minSize = 0,
  onDrop,
  onError,
  src,
  multiple = true,
  className,
  children,
}: DropzoneProps) {
  const [files, setFiles] = useState<File[] | undefined>(src);
  const inputRef = useRef<HTMLInputElement | null>(null);

  useEffect(() => {
    setFiles(src);
  }, [src]);

  const validateAndSet = useCallback(
    (incoming: File[]) => {
      try {
        if (!incoming || incoming.length === 0) return;
        if (maxFiles && incoming.length > maxFiles) {
          throw new Error(`最多上传 ${maxFiles} 个文件`);
        }
        const accepted: File[] = [];
        for (const f of incoming) {
          if (f.size < minSize) throw new Error(`文件 ${f.name} 太小`);
          if (f.size > maxSize) throw new Error(`文件 ${f.name} 超过大小限制`);
          if (accept && Object.keys(accept).length > 0) {
            const allowed = Object.keys(accept).some((pattern) => {
              if (pattern === "image/*") return f.type.startsWith("image/");
              return f.type === pattern;
            });
            if (!allowed) throw new Error(`文件 ${f.name} 类型不被允许`);
          }
          accepted.push(f);
        }
        const newFiles = multiple ? [...(files ?? []), ...accepted] : accepted.slice(0, 1);
        setFiles(newFiles);
        onDrop?.(newFiles);
      } catch (err) {
        onError?.(err as Error);
      }
    },
    [accept, maxFiles, maxSize, minSize, multiple, onDrop, onError, files]
  );

  const handleFiles = useCallback(
    (fileList: FileList | null) => {
      if (!fileList) return;
      const arr = Array.from(fileList);
      validateAndSet(arr);
    },
    [validateAndSet]
  );

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      e.stopPropagation();
      const dt = e.dataTransfer;
      handleFiles(dt.files);
    },
    [handleFiles]
  );

  const handleSelectClick = useCallback(() => {
    inputRef.current?.click();
  }, []);

  const removeFile = useCallback((index: number) => {
    setFiles((prev) => {
      if (!prev) return prev;
      const next = prev.slice();
      next.splice(index, 1);
      onDrop?.(next);
      return next;
    });
  }, [onDrop]);

  const clear = useCallback(() => {
    setFiles(undefined);
    onDrop?.([]);
  }, [onDrop]);

  // generate previews for images
  const previews = React.useMemo(() => {
    if (!files) return [];
    return files.map((f) => {
      if (f.type.startsWith("image/")) {
        return URL.createObjectURL(f);
      }
      return "";
    });
  }, [files]);

  useEffect(() => {
    return () => {
      // revoke object URLs
      previews.forEach((p) => p && URL.revokeObjectURL(p));
    };
  }, [previews]);

  return (
    <DropzoneContext.Provider value={{ files, previews, removeFile, clear }}>
      <div
        className={cn(
          "border border-dashed rounded-md p-4 relative hover:bg-muted/5",
          className
        )}
        onDrop={handleDrop}
        onDragOver={(e) => e.preventDefault()}
      >
        <input
          ref={inputRef}
          type="file"
          className="hidden"
          multiple={multiple}
          accept={accept ? Object.keys(accept).join(",") : undefined}
          onChange={(e) => handleFiles(e.target.files)}
        />
        <div className="flex items-center justify-between gap-4">
          <div className="flex-1">{children}</div>
          <div>
            <Button variant="outline" size="sm" onClick={handleSelectClick}>
              <UploadCloud className="me-2" />
              选择文件
            </Button>
          </div>
        </div>
      </div>
    </DropzoneContext.Provider>
  );
}

export function DropzoneEmptyState() {
  return (
    <div className="flex items-center gap-4">
      <div className="rounded-md bg-muted/10 p-3">
        <UploadCloud />
      </div>
      <div>
        <div className="font-medium">将文件拖放到此处，或点击选择文件上传</div>
        <div className="text-sm text-muted-foreground">支持多文件上传，图片会显示预览</div>
      </div>
    </div>
  );
}

export function DropzoneContent() {
  const ctx = React.useContext(DropzoneContext);
  if (!ctx) return null;
  const { files, previews, removeFile, clear } = ctx;
  if (!files || files.length === 0) {
    return null;
  }
  return (
    <div className="mt-4">
      <div className="flex items-center justify-between mb-2">
        <div className="text-sm text-muted-foreground">已选择文件</div>
        <div>
          <Button variant="ghost" size="sm" onClick={clear}>
            清除
          </Button>
        </div>
      </div>
      <ul className="space-y-2">
        {files.map((f, i) => (
          <li key={f.name + "_" + i} className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-md bg-muted/10 flex items-center justify-center overflow-hidden">
              {previews[i] ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img src={previews[i]} alt={f.name} className="w-full h-full object-cover" />
              ) : (
                <ImageIcon />
              )}
            </div>
            <div className="flex-1">
              <div className="font-medium">{f.name}</div>
              <div className="text-xs text-muted-foreground">{(f.size / 1024).toFixed(1)} KB</div>
            </div>
            <div>
              <Button variant="ghost" size="icon" onClick={() => removeFile(i)}>
                <X />
              </Button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default Dropzone;



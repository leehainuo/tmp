import { useCallback } from 'react'
import { useMutation } from '@tanstack/react-query'
import { api } from '@/lib/api/client'

type FileInfo = {
  url?: string
  path?: string
  [k: string]: unknown
}

type UseFileUploadOptions = {
  endpoint?: string
  fieldName?: string
  onSuccess?: (fileInfo: FileInfo) => void
  onError?: (err: unknown) => void
}

export function useFileUpload(options?: UseFileUploadOptions) {
  const endpoint = options?.endpoint ?? '/upload'
  const fieldName = options?.fieldName ?? 'file'

  const mutation = useMutation<FileInfo, unknown, File>({
    mutationFn: async (file: File) => {
      const fd = new FormData()
      fd.append(fieldName, file)
      const res = await api.post(endpoint, fd, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      return res as FileInfo
    },
    onSuccess: (data) => {
      options?.onSuccess?.(data)
    },
    onError: (err) => {
      options?.onError?.(err)
    },
  })

  const upload = useCallback(
    async (file: File) => {
      return await mutation.mutateAsync(file)
    },
    [mutation]
  )

  return {
    upload,
    isUploading: mutation.isLoading,
    error: mutation.error,
    data: mutation.data,
  }
}

export default useFileUpload



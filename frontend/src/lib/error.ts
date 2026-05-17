type ApiErrorLike = {
  response?: {
    data?: {
      message?: string
      error?: string
    }
  }
  message?: string
}

export function getErrorMessage(error: unknown, fallback: string) {
  const err = error as ApiErrorLike | null | undefined
  return err?.response?.data?.message
    ?? err?.response?.data?.error
    ?? err?.message
    ?? fallback
}

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

export function normalizeBannedWordError(message: string) {
  if (/금지어|금칙어/i.test(message)) {
    return '금칙어가 포함되어 있습니다.'
  }
  return message
}

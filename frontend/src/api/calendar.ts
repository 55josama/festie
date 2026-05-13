import client from './client'
import { unwrap, unwrapPage } from '../lib/api'
import type { CalendarEntry } from '../types'

export const getCalendars = async (year: number, month: number) => {
  const res = await client.get('/calendar-service/v1/calendars', { params: { year, month } })
  return unwrapPage<CalendarEntry>(res.data)
}

export const createCalendar = async (payload: { memo?: string; eventId: string; eventDate: string }) => {
  const res = await client.post('/calendar-service/v1/calendars', payload)
  return unwrap<CalendarEntry>(res.data)
}

export const updateCalendar = async (calendarId: string, payload: { memo?: string }) => {
  const res = await client.patch(`/calendar-service/v1/calendars/${calendarId}`, payload)
  return unwrap<CalendarEntry>(res.data)
}

export const deleteCalendar = async (calendarId: string) => {
  const res = await client.delete(`/calendar-service/v1/calendars/${calendarId}`)
  return unwrap<void>(res.data)
}

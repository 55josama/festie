import client from './client'
import { unwrap, unwrapPage } from '../lib/api'
import type { Event } from '../types'

export const getEvents = async (params: Record<string, any> = {}) => {
  const res = await client.get('/event-service/v1/events', { params })
  return unwrapPage<Event>(res.data)
}

export const getEvent = async (eventId: string) => {
  const res = await client.get(`/event-service/v1/events/${eventId}`)
  return unwrap<Event>(res.data)
}

export const getWeeklyEvents = () => getEvents({ size: 4, sort: 'startAt,asc' })
export const getTicketingEvents = () => getEvents({ size: 4, sort: 'ticketingOpenAt,asc', hasTicketing: true })

export const createEvent = async (payload: Record<string, any>) => {
  const res = await client.post('/event-service/v1/events', payload)
  return unwrap<Event>(res.data)
}

export const deleteEvent = async (eventId: string) => {
  const res = await client.delete(`/event-service/v1/events/${eventId}`)
  return unwrap<void>(res.data)
}

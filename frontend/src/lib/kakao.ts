type AddressSearchResult = {
  address: string
  region: string
  latitude: number | null
  longitude: number | null
}

type DaumPostcodeResult = {
  roadAddress: string
  jibunAddress: string
  address: string
  zonecode: string
}

type KakaoAddressDocument = {
  address_name: string
  x: string
  y: string
  address?: {
    region_1depth_name?: string
    region_2depth_name?: string
    region_3depth_name?: string
  }
}

type KakaoRegionDocument = {
  region_1depth_name?: string
  region_2depth_name?: string
  region_3depth_name?: string
}

declare global {
  interface Window {
    daum?: {
      Postcode: new (options: { oncomplete: (result: DaumPostcodeResult) => void }) => { open: () => void }
    }
    kakao?: {
      maps?: {
        load: (callback: () => void) => void
        services?: {
          Geocoder: new () => {
            addressSearch: (address: string, callback: (result: KakaoAddressDocument[], status: string) => void) => void
            coord2RegionCode: (
              longitude: number,
              latitude: number,
              callback: (result: KakaoRegionDocument[], status: string) => void,
            ) => void
          }
        }
      }
    }
  }
}

const scriptCache = new Map<string, Promise<void>>()

function loadScript(src: string, id: string) {
  const cached = scriptCache.get(id)
  if (cached) return cached

  const promise = new Promise<void>((resolve, reject) => {
    const existing = document.getElementById(id) as HTMLScriptElement | null
    if (existing) {
      if (existing.dataset.loaded === 'true') {
        resolve()
        return
      }
      existing.addEventListener('load', () => resolve(), { once: true })
      existing.addEventListener('error', () => reject(new Error(`Failed to load ${src}`)), { once: true })
      return
    }

    const script = document.createElement('script')
    script.id = id
    script.src = src
    script.async = true
    script.defer = true
    script.addEventListener(
      'load',
      () => {
        script.dataset.loaded = 'true'
        resolve()
      },
      { once: true },
    )
    script.addEventListener('error', () => reject(new Error(`Failed to load ${src}`)), { once: true })
    document.head.appendChild(script)
  })

  scriptCache.set(id, promise)
  return promise
}

function getKakaoKey() {
  return import.meta.env.VITE_KAKAO_JS_KEY as string | undefined
}

function normalizeRegionName(value?: string) {
  if (!value) return ''
  const compact = value.replace(/\s+/g, '')
  if (/서울/.test(compact)) return '서울'
  if (/경기/.test(compact)) return '경기'
  if (/세종|대전|충청/.test(compact)) return '충청'
  if (/강원/.test(compact)) return '강원'
  if (/경상|부산|대구|울산|창원|포항|경주|진주|구미|경산/.test(compact)) return '경상'
  if (/전라|광주|전주|목포|여수|순천|군산|익산/.test(compact)) return '전라'
  if (/부산/.test(compact)) return '부산'
  if (/제주/.test(compact)) return '제주'
  return compact
}

function compactRegionLabel(value?: string) {
  const normalized = normalizeRegionName(value)
  if (!normalized) return ''
  return normalized.length <= 2 ? normalized : normalized.slice(0, 2)
}

let postcodePromise: Promise<void> | null = null
let kakaoPromise: Promise<void> | null = null

export async function ensureDaumPostcode() {
  if (window.daum?.Postcode) return
  if (!postcodePromise) {
    postcodePromise = loadScript('https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js', 'festie-daum-postcode')
    postcodePromise.catch(() => { postcodePromise = null })
  }
  await postcodePromise
}

export async function ensureKakaoMaps() {
  if (window.kakao?.maps?.services) return
  const kakaoKey = getKakaoKey()
  if (!kakaoKey) {
    throw new Error('VITE_KAKAO_JS_KEY가 설정되지 않았습니다.')
  }
  if (!kakaoPromise) {
    kakaoPromise = new Promise<void>((resolve, reject) => {
      // 이미 로드된 스크립트가 있으면 재사용
      const existing = document.getElementById('festie-kakao-maps') as HTMLScriptElement | null
      if (existing) {
        // 이미 로드 완료 + services 사용 가능
        if (existing.dataset.loaded === 'true' && window.kakao?.maps?.services) {
          resolve()
          return
        }
        // 이미 로드 완료됐지만 services 없음 → 스크립트 제거 후 재로드
        if (existing.dataset.loaded === 'true') {
          existing.remove()
        } else {
          // 아직 로딩 중이면 이벤트 기다림
          existing.addEventListener('load', () => { window.kakao?.maps?.load(() => resolve()) }, { once: true })
          existing.addEventListener('error', () => reject(new Error('Kakao maps SDK를 불러오지 못했습니다.')), { once: true })
          return
        }
      }

      const script = document.createElement('script')
      script.id = 'festie-kakao-maps'
      script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoKey}&libraries=services&autoload=false`
      script.async = true
      script.defer = true
      script.addEventListener(
        'load',
        () => {
          script.dataset.loaded = 'true'
          window.kakao?.maps?.load(() => resolve())
        },
        { once: true },
      )
      script.addEventListener('error', () => reject(new Error('Kakao maps SDK를 불러오지 못했습니다.')), { once: true })
      document.head.appendChild(script)
    })
    // 실패 시 캐시 초기화 → 다음 호출 때 재시도 가능
    kakaoPromise.catch(() => { kakaoPromise = null })
  }
  await kakaoPromise
}

export async function searchAddressWithKakao(): Promise<AddressSearchResult> {
  await ensureDaumPostcode()

  const selected = await new Promise<DaumPostcodeResult>((resolve, reject) => {
    if (!window.daum?.Postcode) {
      reject(new Error('Daum Postcode를 불러오지 못했습니다.'))
      return
    }

    const postcode = new window.daum.Postcode({
      oncomplete: (data) => resolve(data),
    })
    postcode.open()
  })

  const address = selected.roadAddress || selected.jibunAddress || selected.address
  if (!address) {
    throw new Error('주소를 선택하지 못했습니다.')
  }

  try {
    await ensureKakaoMaps()
    const geocoder = new window.kakao!.maps!.services!.Geocoder()
    const candidates = buildGeocodeCandidates(selected)
    const geo = await geocodeFirstMatch(geocoder, candidates)

    return {
      address,
      region: compactRegionLabel(geo?.region ?? address),
      latitude: geo?.latitude ?? null,
      longitude: geo?.longitude ?? null,
    }
  } catch {
    return {
      address,
      region: compactRegionLabel(address),
      latitude: null,
      longitude: null,
    }
  }
}

export async function resolveRegionFromCoordinates(latitude: number, longitude: number) {
  await ensureKakaoMaps()
  const geocoder = new window.kakao!.maps!.services!.Geocoder()
  return await new Promise<string | null>((resolve) => {
    geocoder.coord2RegionCode(longitude, latitude, (result, status) => {
      if (status !== 'OK' || !result[0]) {
        resolve(null)
        return
      }
      resolve(compactRegionLabel(result[0].region_1depth_name))
    })
  })
}

async function geocodeFirstMatch(
  geocoder: InstanceType<NonNullable<NonNullable<NonNullable<typeof window.kakao>['maps']>['services']>['Geocoder']>,
  candidates: Array<string | undefined>,
) {
  for (const candidate of candidates) {
    const address = candidate?.trim()
    if (!address) continue

    const match = await new Promise<{ region: string; latitude: number; longitude: number } | null>((resolve) => {
      geocoder.addressSearch(address, (result, status) => {
        if (status !== 'OK' || !result[0]) {
          resolve(null)
          return
        }

        const doc = result[0]
        const latitude = Number(doc.y)
        const longitude = Number(doc.x)
        if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
          resolve(null)
          return
        }

        resolve({
          region: normalizeRegionName(doc.address?.region_1depth_name ?? doc.address_name),
          latitude,
          longitude,
        })
      })
    })

    if (match) return match
  }

  return null
}

function buildGeocodeCandidates(selected: DaumPostcodeResult) {
  const raw = [selected.roadAddress, selected.jibunAddress, selected.address]
  const normalized = raw
    .flatMap((value) => {
      const trimmed = value?.trim()
      if (!trimmed) return []

      const candidates = [trimmed]
      const stripped = trimmed
        .replace(/\s*\([^)]*\)\s*/g, ' ')
        .replace(/\s+/g, ' ')
        .trim()
      if (stripped && stripped !== trimmed) {
        candidates.push(stripped)
      }

      return candidates
    })
    .filter((value, index, array) => array.indexOf(value) === index)

  return normalized.length ? normalized : [selected.address]
}

import './JobMap.css';

const locations = [
  {
    area: '강남',
    station: '강남역',
    address: '서울 강남구 테헤란로 일대',
    lat: 37.4979,
    lng: 127.0276,
  },
  {
    area: '판교',
    station: '판교역',
    address: '경기 성남시 분당구 판교역로 일대',
    lat: 37.3947,
    lng: 127.1112,
  },
  {
    area: '구로',
    station: '구로디지털단지역',
    address: '서울 구로구 디지털로 일대',
    lat: 37.4852,
    lng: 126.9015,
  },
  {
    area: '성수',
    station: '성수역',
    address: '서울 성동구 성수이로 일대',
    lat: 37.5446,
    lng: 127.0557,
  },
  {
    area: '마곡',
    station: '마곡나루역',
    address: '서울 강서구 마곡중앙로 일대',
    lat: 37.5602,
    lng: 126.8358,
  },
];

function locationForJob(job, index) {
  const text = `${job?.companyName || ''} ${job?.title || ''} ${job?.position || ''}`.toLowerCase();

  if (text.includes('판교') || text.includes('스타트업') || text.includes('넥스트')) return locations[1];
  if (text.includes('구로') || text.includes('테크') || text.includes('세이프')) return locations[2];
  if (text.includes('성수') || text.includes('인터랙션') || text.includes('웹프렌즈')) return locations[3];
  if (text.includes('마곡') || text.includes('클라우드')) return locations[4];

  return locations[index % locations.length];
}

function googleMapUrl(location, zoom = 11) {
  return `https://maps.google.com/maps?q=${location.lat},${location.lng}&z=${zoom}&output=embed`;
}

function JobMap({ jobs, selectedJob, onSelect }) {
  const selectedIndex = Math.max(0, jobs.findIndex((job) => job.id === selectedJob?.id));
  const currentJob = jobs[selectedIndex];
  const selectedLocation = locationForJob(currentJob, selectedIndex);

  const moveMapSelection = (direction) => {
    if (!jobs.length) return;
    const nextIndex = (selectedIndex + direction + jobs.length) % jobs.length;
    onSelect(nextIndex);
  };

  return (
    <div className="job-real-map">
      <iframe
        className="google-map-frame"
        src={googleMapUrl(selectedLocation, selectedJob ? 13 : 10)}
        title="채용공고 위치 지도"
        loading="lazy"
        referrerPolicy="no-referrer-when-downgrade"
      />

      <div className="map-info-panel">
        <span>채용공고 위치</span>
        <strong>{currentJob?.companyName || '공고 선택'} · {selectedLocation.area}</strong>
        <p>{selectedLocation.address}</p>
        <small>{selectedLocation.station} 근처</small>
      </div>

      <button className="map-side-arrow left" type="button" onClick={() => moveMapSelection(-1)} aria-label="이전 위치">
        ‹
      </button>
      <button className="map-side-arrow right" type="button" onClick={() => moveMapSelection(1)} aria-label="다음 위치">
        ›
      </button>

      <div className="map-bottom-status">
        <strong>MATCH #{jobs.length ? selectedIndex + 1 : 0}</strong>
        <span>· {jobs.length ? selectedIndex + 1 : 0} / {jobs.length}</span>
      </div>
    </div>
  );
}

export default JobMap;

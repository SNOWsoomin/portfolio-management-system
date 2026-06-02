import React, { useState } from 'react';
import './BookmarkIcon.css';
import bookmarkUnfilled from '../assets/bookmark.png';
import bookmarkFilled from '../assets/bookmark_.png';

function BookmarkIcon({ isMini = false }) {
  const [active, setActive] = useState(false);

  const toggleBookmark = () => {
    setActive(!active);
  };

  const iconSrc = active ? bookmarkFilled : bookmarkUnfilled;
  const iconAlt = active ? 'Bookmarked' : 'Unbookmarked';
  const iconClass = `bookmark-icon ${isMini ? 'mini' : ''} ${active ? 'active' : ''}`;

  return (
    <div className={iconClass} onClick={toggleBookmark}>
      <img src={iconSrc} alt={iconAlt} />
    </div>
  );
}

export default BookmarkIcon;
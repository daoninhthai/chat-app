import React, { useState, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import api from '../../services/api';
import './RoomList.css';

const RoomList = ({ currentRoomId }) => {
  const [rooms, setRooms] = useState([]);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newRoomName, setNewRoomName] = useState('');
  const [newRoomDesc, setNewRoomDesc] = useState('');
  const [loading, setLoading] = useState(false);
  const history = useHistory();

  // load danh sach phong chat
  useEffect(() => {
    loadRooms();
  }, []);

  const loadRooms = async () => {
    try {
      const response = await api.get('/chat/rooms');
      setRooms(response.data);
    } catch (error) {
      console.error('Loi khi load danh sach phong:', error);
      // du lieu mac dinh neu server chua co API
      setRooms([
        { id: 1, name: 'General', description: 'Phong chat chung' },
        { id: 2, name: 'Tech Talk', description: 'Thao luan ve cong nghe' },
        { id: 3, name: 'Random', description: 'Noi chuyen linh tinh' },
      ]);
    }
  };

  const handleJoinRoom = (roomId) => {
    history.push(`/chat/${roomId}`);
  };

  const handleCreateRoom = async (e) => {
    e.preventDefault();

    if (!newRoomName.trim()) return;

    setLoading(true);
    try {
      const response = await api.post('/chat/rooms', {
        name: newRoomName.trim(),
        description: newRoomDesc.trim(),
      });

      setRooms((prev) => [...prev, response.data]);
      setNewRoomName('');
      setNewRoomDesc('');
      setShowCreateForm(false);

      // tu dong vao phong vua tao
      history.push(`/chat/${response.data.id}`);
    } catch (error) {
      console.error('Loi khi tao phong:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="room-list-container">
      <div className="room-list-header">
        <h3>Phong Chat</h3>
        <button
          className="btn-new-room"
          onClick={() => setShowCreateForm(!showCreateForm)}
          title="Tao phong moi"
        >
          +
        </button>
      </div>

      {showCreateForm && (
        <form className="create-room-form" onSubmit={handleCreateRoom}>
          <input
            type="text"
            placeholder="Ten phong..."
            value={newRoomName}
            onChange={(e) => setNewRoomName(e.target.value)}
            required
            autoFocus
          />
          <input
            type="text"
            placeholder="Mo ta (khong bat buoc)..."
            value={newRoomDesc}
            onChange={(e) => setNewRoomDesc(e.target.value)}
          />
          <div className="create-room-actions">
            <button type="submit" disabled={loading}>
              {loading ? '...' : 'Tao'}
            </button>
            <button
              type="button"
              className="btn-cancel"
              onClick={() => setShowCreateForm(false)}
            >
              Huy
            </button>
          </div>
        </form>
      )}

      <div className="room-items">
        {rooms.map((room) => (
          <div
            key={room.id}
            className={`room-item ${
              String(room.id) === String(currentRoomId) ? 'active' : ''
            }`}
            onClick={() => handleJoinRoom(room.id)}
          >
            <div className="room-item-name"># {room.name}</div>
            {room.description && (
              <div className="room-item-desc">{room.description}</div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default RoomList;

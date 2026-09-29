-- 创建 Z23 软件七班学生登录账号
-- 用户名使用学号，默认密码 123456，角色为学生用户（role_id=3）。
SET NAMES utf8mb4;
START TRANSACTION;

ALTER TABLE sys_user MODIFY COLUMN username varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学号/登录账号';

INSERT INTO sys_user (username, password, nickname, role_id, sex, status, del_flag, remark) VALUES
('Z2025725739', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '李阳冬', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725710', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '周情', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725701', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '王丹', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725713', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '唐欢欢', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725720', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '李笑飞', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725742', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '罗林波', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725728', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '罗宇恒', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725724', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '陈炯宏', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725734', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '赵文森', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725730', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '刘儒华', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725707', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '蔡雨晴', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725704', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '蒋双双', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725714', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '高玲', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725732', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '朱刘杰', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725712', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '胡云霞', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725709', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '王晶', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725706', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '周玲玲', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725727', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '桂润禾', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725741', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '任轩辰', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725711', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '万佩佳', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725705', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '龚琪雅', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725731', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '吴江勇', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725719', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '陈仙', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725736', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '万林', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725737', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '冯俊杰', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725726', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '杨晨宇', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725729', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '李俊宏', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725717', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '王天姿', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725740', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '葛晔琅', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725725', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '赵国润', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725723', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '巨祥志', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725708', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '顾佩佩', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725715', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '汪洪燕', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725718', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '潘婷婷', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725733', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '田帅', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725703', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '肖春灿', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725722', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '冉建康', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725744', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '王酉海', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725721', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '杨擎', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725735', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '张嘉乐', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725716', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '陶曼凌', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725702', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '孙亮', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725738', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '吴昊俣', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456'),
('Z2025725743', '$2b$10$zhkHi2CAcPiuAbRCZ9SvY.dGwkLfzLJesiKDniqBDVH/cmA.JwyRi', '梁佳豪', 3, '2', '0', '0', 'Z23软件七班学生账号，初始密码123456')
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  nickname = VALUES(nickname),
  role_id = VALUES(role_id),
  status = VALUES(status),
  del_flag = VALUES(del_flag),
  remark = VALUES(remark);

COMMIT;

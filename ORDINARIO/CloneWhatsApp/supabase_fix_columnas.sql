-- =========================================================================
-- CLONEWHATSAPP: REPARACIÓN PARA BASES QUE YA TENÍAN TABLAS ANTERIORES
-- Solo AGREGA lo que falta. No borra tablas ni datos.
-- Se puede ejecutar varias veces sin problema.
-- =========================================================================

-- 1. Columnas que la app necesita y no existían
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS last_seen TIMESTAMP WITH TIME ZONE DEFAULT now();
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS is_typing BOOLEAN DEFAULT false;

ALTER TABLE public.devices ADD COLUMN IF NOT EXISTS fcm_token TEXT;
ALTER TABLE public.devices ADD COLUMN IF NOT EXISTS device_type TEXT DEFAULT 'android';
ALTER TABLE public.devices ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL;

ALTER TABLE public.conversaciones ADD COLUMN IF NOT EXISTS is_group BOOLEAN DEFAULT false;
ALTER TABLE public.conversaciones ADD COLUMN IF NOT EXISTS name TEXT DEFAULT '';

ALTER TABLE public.messages ADD COLUMN IF NOT EXISTS sender_name TEXT DEFAULT '';
ALTER TABLE public.messages ADD COLUMN IF NOT EXISTS image_url TEXT DEFAULT '';
ALTER TABLE public.messages ADD COLUMN IF NOT EXISTS status TEXT NOT NULL DEFAULT 'sent';

-- 2. Índice único que usa la app para guardar el token FCM (upsert por user_id + fcm_token)
CREATE UNIQUE INDEX IF NOT EXISTS devices_user_id_fcm_token_key ON public.devices(user_id, fcm_token);

-- 3. Columnas viejas que la app NO envía: si son obligatorias (NOT NULL) y no tienen
--    valor por defecto, harían fallar los INSERT. Se vuelven opcionales.
DO $$
DECLARE
    r RECORD;
    app_cols TEXT[] := ARRAY[
        'profiles.id','profiles.username','profiles.avatar_url','profiles.last_seen','profiles.is_typing','profiles.created_at',
        'devices.id','devices.user_id','devices.fcm_token','devices.device_type','devices.updated_at',
        'conversaciones.id','conversaciones.is_group','conversaciones.name','conversaciones.created_by','conversaciones.created_at',
        'conversaciones_members.conversacion_id','conversaciones_members.user_id','conversaciones_members.joined_at',
        'messages.id','messages.conversacion_id','messages.sender_id','messages.sender_name','messages.content',
        'messages.message_type','messages.image_url','messages.status','messages.created_at'
    ];
BEGIN
    FOR r IN
        SELECT c.table_name, c.column_name
        FROM information_schema.columns c
        WHERE c.table_schema = 'public'
          AND c.table_name IN ('profiles','devices','conversaciones','conversaciones_members','messages')
          AND c.is_nullable = 'NO'
          AND c.column_default IS NULL
          AND NOT ((c.table_name || '.' || c.column_name) = ANY (app_cols))
          AND c.column_name NOT IN (
              SELECT kcu.column_name FROM information_schema.table_constraints tc
              JOIN information_schema.key_column_usage kcu
                ON tc.constraint_name = kcu.constraint_name AND tc.table_name = kcu.table_name
              WHERE tc.table_schema = 'public' AND tc.table_name = c.table_name
                AND tc.constraint_type = 'PRIMARY KEY')
    LOOP
        EXECUTE format('ALTER TABLE public.%I ALTER COLUMN %I DROP NOT NULL', r.table_name, r.column_name);
        RAISE NOTICE 'Columna vuelta opcional: %.%', r.table_name, r.column_name;
    END LOOP;
END $$;

-- 4. Permisos y recarga del esquema de la API
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO authenticated;
NOTIFY pgrst, 'reload schema';

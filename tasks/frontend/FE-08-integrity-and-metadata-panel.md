# Tarea FE-08: Panel de auditoría de integridad y visor de metadatos técnicos de DFSha

## 1. Identificación
- **ID:** FE-08
- **Título:** Panel de auditoría de integridad del clúster y visor de metadatos técnicos de archivos
- **Dominio:** Frontend
- **Tipo:** Integración / Funcionalidad faltante / UI
- **Prioridad:** Media

---

## 2. Problema
### Estado actual
El backend de la arquitectura distribuida DFSha cuenta con dos capacidades avanzadas clave orientadas a la alta disponibilidad y la transparencia:

1. **Auditoría y auto-sanación de integridad (`POST /api/files/check-integrity`):**
   Ejecuta `CheckFilesUseCase` para inspeccionar la integridad de los chunks distribuidos en los nodos de almacenamiento, reportando el total de archivos auditados, archivos sanos, archivos auto-recuperados (*healed*) y archivos corruptos. La función cliente `checkFileIntegrity` ya se encuentra implementada en `ClientFrontEnd/src/services/fileService.ts:92-103`.
2. **Inspección de metadatos técnicos (`GET /api/files/{id}`):**
   Ejecuta `GetMetadataFileUseCase` y devuelve información como el número total de particiones (*chunks*), tamaño en bytes, tipo y fecha de modificación. La función cliente `getFileMetadata` ya existe en `fileService.ts:44-57`.

Sin embargo, en el Frontend:
- **No existe ninguna interfaz ni botón para solicitar la comprobación de integridad.** Esta capacidad esencial del sistema distribuido está totalmente desaprovechada en la capa visual.
- **No existe ningún modal o panel lateral de detalles técnicos.** El usuario o administrador no puede inspeccionar cuántas particiones componen su archivo ni verificar su consistencia.

### Impacto
Una de las propuestas de valor más importantes del proyecto (la tolerancia a fallos, replicación por chunks y auto-sanación del clúster) permanece oculta y no demostrable desde la aplicación web.

---

## 3. Ubicación
- `ClientFrontEnd/src/components/folders/FileExplorer.tsx`
- `ClientFrontEnd/src/components/folders/AsideButtonsFileExplorer.tsx`
- `ClientFrontEnd/src/services/fileService.ts`
- `ClientFrontEnd/src/types/fileType.ts`

---

## 4. Objetivo
Proveer una interfaz gráfica para consultar los metadatos técnicos de un archivo seleccionado (número de particiones, identificador único, tamaño exacto y fecha) y una vista o modal de diagnóstico para disparar y visualizar el reporte de auditoría de integridad del sistema de archivos distribuido.

---

## 5. Requisitos
- **Modal de Metadatos de Archivo (`FileMetadataModal.tsx`):**
  - Incorporar una opción de "Ver detalles" o "Información" en las acciones de cada fila de archivo en `FileBlock.tsx`.
  - Al seleccionarla, consultar `getFileMetadata(fileId)` y presentar en un diálogo modal estilizado:
    - Nombre del archivo y extensión.
    - Identificador UUID en el sistema.
    - Tamaño exacto en bytes y formateado.
    - Total de particiones / chunks en los que está fragmentado en el clúster (`totalPartitions`).
    - Fecha de creación y última modificación.
- **Acción y Reporte de Integridad del Clúster (`ClusterIntegrityModal.tsx`):**
  - Conectar el botón "Ver más" (o un nuevo botón "Auditoría de Integridad" / "Estado del Clúster") en la barra lateral `AsideButtonsFileExplorer.tsx`.
  - Al abrirse, permitir iniciar un chequeo con un botón "Ejecutar comprobación de integridad".
  - Desplegar mientras se ejecuta un estado de análisis en curso.
  - Al responder el backend, renderizar el reporte visual:
    - Indicador general de salud del clúster (Verde: 100% íntegro, Amarillo: auto-sanado, Rojo: bloques corruptos).
    - Tarjetas con métricas: Archivos auditados, Archivos saludables, Bloques sanados (*healed*) y Archivos corruptos.
    - Lista detallada de incidencias si existen archivos con problemas (`issues`).

---

## 6. Alcance
- **Cambios necesarios:**
  - Creación de los componentes `FileMetadataModal.tsx` y `ClusterIntegrityModal.tsx`.
  - Integración del disparador de metadatos en `FileBlock.tsx`.
  - Integración del disparador de auditoría en `AsideButtonsFileExplorer.tsx` y `FileExplorerPage.tsx`.
  - Creación de los estilos CSS correspondientes respetando las variables de diseño.
- **Restricciones:**
  - La ejecución de auditoría de integridad puede restringirse visualmente o indicarse como función de diagnóstico del sistema.

---

## 7. Criterios de aceptación
- El usuario puede hacer clic en "Ver detalles" en cualquier archivo y visualizar el modal con su UUID, número de chunks distribuidos y fechas exactas.
- El usuario puede presionar el botón de auditoría en la barra lateral y disparar `POST /api/files/check-integrity`.
- El reporte de integridad se despliega con tarjetas de resumen legibles y comprensibles.
- Si se detecta un error de comunicación o timeout, el modal ofrece un mensaje de reintento claro.

---

## 8. Pruebas
1. **Consulta de metadatos:** Abrir los detalles de un archivo de prueba; verificar llamada a `GET /api/files/{id}` y que el total de particiones coincida con el valor de base de datos.
2. **Ejecución de auditoría:** Presionar "Ejecutar comprobación de integridad"; validar en la pestaña Red la solicitud a `/api/files/check-integrity` y comprobar que los contadores del reporte se rendericen correctamente.
3. **Manejo de estado de carga:** Verificar que mientras se computa el reporte de integridad se muestre un spinner o indicador de progreso activo.

---

## 9. Dependencias
- **Tareas previas:** FE-01, FE-02, FE-04.
- **Tareas relacionadas:** FE-06 (navegación lateral).


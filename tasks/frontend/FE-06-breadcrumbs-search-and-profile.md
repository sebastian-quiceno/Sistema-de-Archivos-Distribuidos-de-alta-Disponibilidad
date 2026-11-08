# Tarea FE-06: Navegación jerárquica con Breadcrumbs, Búsqueda y Menú de Perfil de Usuario

## 1. Identificación
- **ID:** FE-06
- **Título:** Navegación jerárquica con Breadcrumbs, filtro de búsqueda en tiempo real y menú de perfil de usuario
- **Dominio:** Frontend
- **Tipo:** Funcionalidad incompleta / UX
- **Prioridad:** Alta

---

## 2. Problema
### Estado actual
La experiencia global en la vista principal del explorador (`FileExplorerPage.tsx` y `FileExplorer.tsx`) presenta vacíos funcionales significativos:

1. **Anti-patrón de React en `UserPerfileButton.tsx`:**
   ```tsx
   export function UserPerfileButton() {
     const {auth, logout} = useAuth();
     
     //Que hacer cuando no hay sesion
     if(!auth){
       logout();
     }
     ...
   ```
   Invocar una función de actualización de estado (`logout()`) directamente dentro del cuerpo de renderizado de un componente React cuando `!auth` es un efecto secundario prohibido que puede causar bucles infinitos de renderizado y advertencias en modo estricto.

2. **Botón de usuario sin interacción:**
   El botón con el icono de usuario `<button className='profile__button' type="button">` no cuenta con ningún listener `onClick`. El usuario no dispone de ningún menú desplegable ni mecanismo visible para cerrar sesión, ver su identificador de usuario o gestionar su cuenta.

3. **Placeholder no implementado de búsqueda:**
   En `FileExplorerPage.tsx:50` se encuentra una cadena hardcodeada:
   ```tsx
   <div className="file-explorer-page__main__search">AGREGAR BARRA BUSQUEDA</div>
   ```
   No existe ningún campo de entrada (`<input type="search">`) ni lógica para filtrar los archivos y carpetas del directorio activo por nombre.

4. **Ausencia de navegación por migas de pan (Breadcrumbs):**
   El explorador de archivos solo muestra como título `"Mi Unidad"` o `"Carpeta"`. Cuando el usuario navega a 2 o más niveles de profundidad dentro de carpetas anidadas, no tiene indicación visual de la ruta en la que se encuentra ni forma de saltar a carpetas antecesoras intermedias, dependiendo únicamente del botón "Subir de carpeta" paso a paso.

5. **Botón "Ver más" inactivo:**
   En `AsideButtonsFileExplorer.tsx`, el botón "Ver más" espera una propiedad `onOpenMore`, pero `FileExplorerPage` no se la proporciona, dejando el botón inerte.

### Impacto
El usuario queda desorientado dentro de árboles de directorios profundos, no puede buscar elementos rápidamente y no tiene forma intuitiva y segura de cerrar su sesión o consultar su identidad de usuario.

---

## 3. Ubicación
- `ClientFrontEnd/src/app/pages/FileExplorerPage.tsx`
- `ClientFrontEnd/src/components/folders/FileExplorer.tsx`
- `ClientFrontEnd/src/components/users/UserPerfileButton.tsx`
- `ClientFrontEnd/src/components/folders/AsideButtonsFileExplorer.tsx`
- `ClientFrontEnd/src/styles/pages/FileExplorerPage.css`
- `ClientFrontEnd/src/styles/user/UserPerfileButton.css`

---

## 4. Objetivo
Proveer una experiencia de usuario completa y ergonómica en el explorador: barra de migas de pan (breadcrumbs) con navegación directa a niveles superiores, un componente de búsqueda que filtre elementos en tiempo real, un menú desplegable de usuario con cierre de sesión seguro sin efectos secundarios en el render, y un manejo limpio de las acciones secundarias de la barra lateral.

---

## 5. Requisitos
- **Corrección y menú en `UserPerfileButton.tsx`:**
  - Eliminar el llamado `logout()` en la fase de renderizado.
  - Implementar un menú flotante accesible (dropdown) que se abra/cierre al hacer clic en el botón de perfil o en el nombre de usuario.
  - El menú debe mostrar el nombre del usuario (`userName`), su `userId` (o rol) y un botón destacado "Cerrar sesión" que invoque `logout()` y redirija a `/signin`.
  - Soportar cierre automático del dropdown al hacer clic fuera del componente (Click Outside) o con la tecla `Escape`.
- **Implementación de Breadcrumbs (Migas de pan):**
  - Mantener en el estado de `FileExplorer` (o en la URL mediante query params o rutas) el historial de la ruta actual `[{ id: rootFolderId, name: "Mi Unidad" }, { id: folder1, name: "Docs" }, ...]`.
  - Renderizar una barra horizontal clicable: `Mi Unidad > Docs > Proyectos`.
  - Al hacer clic en cualquier segmento antecesor, navegar directamente a esa carpeta.
- **Implementación de Barra de Búsqueda:**
  - Reemplazar el `<div>` con texto temporal por un input estilizado de búsqueda con icono de lupa y botón para limpiar texto.
  - Filtrar la lista de archivos (`files`) y carpetas (`folders`) actualmente desplegados de acuerdo al término ingresado (case-insensitive).
  - Mostrar un estado vacío amigable si ningún elemento coincide con la búsqueda (ej: "No se encontraron resultados para '...'").
- **Conexión de la barra lateral:**
  - Decidir el comportamiento del botón "Ver más": abrir un panel lateral con información de almacenamiento del usuario (cuota de disco usada vs libre en el clúster) o remover el botón si no aplica al alcance actual.

---

## 6. Alcance
- **Cambios necesarios:**
  - Refactorización de `UserPerfileButton.tsx` con soporte de menú desplegable.
  - Creación del componente `Breadcrumb.tsx` e integración en `FileExplorer.tsx`.
  - Creación del componente `SearchBar.tsx` e integración en `FileExplorerPage.tsx`.
  - Ajustes de estilos CSS en `FileExplorerPage.css` y `UserPerfileButton.css`.
- **Restricciones:**
  - El filtrado de búsqueda se ejecuta en cliente sobre los elementos cargados en la carpeta actual, manteniendo baja latencia y sin sobrecargar llamadas al backend.

---

## 7. Criterios de aceptación
- `UserPerfileButton` no genera advertencias de renderizado en React. Al hacer clic, se despliega el menú con la información del usuario y la opción de cerrar sesión.
- Hacer clic en "Cerrar sesión" limpia la sesión en `localStorage` y redirige inmediatamente al usuario a `/signin`.
- Al ingresar a una subcarpeta, la barra de migas de pan muestra la ruta jerárquica y permite saltar directamente a cualquier carpeta previa con un solo clic.
- Escribir en la barra de búsqueda filtra instantáneamente las filas de archivos y carpetas visibles; al borrar el término se restablece la lista completa.
- No persisten textos de placeholder como "AGREGAR BARRA BUSQUEDA" en la interfaz.

---

## 8. Pruebas
1. **Menú de perfil y Logout:** Abrir la aplicación con usuario autenticado; presionar el botón de perfil; validar apertura del dropdown; hacer clic en "Cerrar sesión" y comprobar redirección a `/signin` y limpieza de `auth_session`.
2. **Cierre de menú:** Abrir el menú de perfil y presionar la tecla `Escape` o hacer clic fuera de él; verificar que se cierre.
3. **Navegación Breadcrumbs:** Ingresar a `Subcarpeta 1` y luego a `Subcarpeta 2`; comprobar que las migas indiquen `Mi Unidad > Subcarpeta 1 > Subcarpeta 2`. Hacer clic en `Mi Unidad` y confirmar que se cargue la raíz de inmediato.
4. **Filtro de búsqueda:** En una carpeta con varios archivos, escribir parte del nombre de uno de ellos; verificar que la vista filtre únicamente los coincidentes y muestre mensaje adecuado cuando no haya coincidencias.

---

## 9. Dependencias
- **Tareas previas:** FE-01, FE-02.
- **Tareas relacionadas:** FE-04 (archivos), FE-05 (carpetas).


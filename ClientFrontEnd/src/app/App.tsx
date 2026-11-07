import SignUpPage from './pages/SignUpPage'
import {UserPerfileButton} from '../components/users/UserPerfileButton'
import {LogoRow} from '../components/folders/LogoRow'
import {ActionButton} from '../components/folders/ActionButton'
import {File} from '../components/folders/File'
import {DeleteModal} from '../components/folders/DeleteModal'



import './App.css'

function App() {

  return (
    <>
      <SignUpPage/>
      <UserPerfileButton/>
      <LogoRow/>
      <File fileName='Prueba' fileType='folder' />
      <File fileName='Prueba1' fileType='jpg' />
      <File fileName='Prueba2' fileType='mp3' />
      <DeleteModal folderId='86' folderName='prueba' closeMethod={()=> console.log("Se borro la carpeta ")}/>
    </>
    
  )
}

export default App

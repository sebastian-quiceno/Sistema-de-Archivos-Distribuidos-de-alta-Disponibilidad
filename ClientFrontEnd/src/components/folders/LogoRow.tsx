import '../../styles/folder/LogoRow.css'

import logo from '../../assets/AilatanLogo.png'

export function LogoRow(){
    return(
        <div className='logo-row'>
            <img src={logo} alt="logo ailatan" />
            <h2>Ail<span>atan</span></h2>
        </div>
    );
}
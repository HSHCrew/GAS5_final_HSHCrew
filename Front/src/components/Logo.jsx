import altariLogo from '../assets/altari-logo.svg';
import {StyledImage} from "./StyledTag.jsx";

export const Logo = () => {
    return (
        <div style={{height:'100%', width:'100%'}}>
            <StyledImage src={altariLogo} alt="logo" height={'100%'}/>
        </div>
    );
};

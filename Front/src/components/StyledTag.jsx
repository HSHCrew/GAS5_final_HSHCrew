import './style.css';

export const StyledImage = ({
    src,
    alt,
    width = 'fit-content',
    height = 'fit-content',
}) => {
    return (
        <img
            className={'styled-image'}
            src={src}
            alt={alt}
            style={{ width: width, height: height }}
        />
    );
};

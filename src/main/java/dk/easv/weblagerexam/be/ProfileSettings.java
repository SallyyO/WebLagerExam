package dk.easv.weblagerexam.be;

public enum ProfileSettings {
    GRAYSCALE,    // convert to grayscale
    ROTATE,       // rotate by fixed degrees (value = degrees)
    ROTATE_AUTO,  // rotate to landscape/horizontal orientation
    BRIGHTEN,     // increase brightness (value = 0-255 increase)

    RAVENCLAW,   // blue
    GRYFFINDOR,  // red
    SLYTHERIN,   // green
    HUFFLEPUFF   // yellow
}

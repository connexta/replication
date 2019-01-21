import React from "react";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import IconButton from "@material-ui/core/IconButton";
import Typography from "@material-ui/core/Typography";
import HomeIcon from "@material-ui/icons/Home";
import LanguageIcon from "@material-ui/icons/Language";
import { Link } from "react-router-dom";

class Navbar extends React.Component {
  render() {
    return (
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" color="inherit" noWrap>
            Replication
          </Typography>
          <div className="navIcons">
            <IconButton component={Link} to="/" color="inherit">
              <HomeIcon />
            </IconButton>
            <IconButton component={Link} to="/sites" color="inherit">
              <LanguageIcon />
            </IconButton>
          </div>
        </Toolbar>
      </AppBar>
    );
  }
}

export default Navbar;

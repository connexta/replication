import React from "react";
import Card from "@material-ui/core/Card";
import CardContent from "@material-ui/core/CardContent";
import PowerIcon from "@material-ui/icons/Power";
import PowerOffIcon from "@material-ui/icons/PowerOff";
import { expandingTile, siteHeader, siteContent } from "./styles.css";
import { CardHeader } from "@material-ui/core";

export default props => {
  const { name, connected, onClick } = props;

  let status;
  if (connected) {
    status = <PowerIcon fontSize="large" />;
  } else {
    status = <PowerOffIcon fontSize="large" color="error" />;
  }

  return (
    <Card onClick={onClick} className={expandingTile}>
      <CardHeader title={name} className={siteHeader} />
      <CardContent className={siteContent}>{status}</CardContent>
    </Card>
  );
};

import Card from '@material-ui/core/Card'
import styled from 'styled-components'

const ExpandingCard = styled(Card)`
  margin: 20px;
  width: 200px;
  height: 200px;

  &:hover {
    margin: 15px;
    width: 210px;
    height: 210px;
    cursor: pointer;
  }
`

export default ExpandingCard

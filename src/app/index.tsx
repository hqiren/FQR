import { Platform, Text, View, Dimensions } from 'react-native';
import { PieChart } from 'react-native-chart-kit';

const chartConfig = {
  backgroundGradientFrom: "#1E2923",
  backgroundGradientFromOpacity: 0,
  backgroundGradientTo: "#08130D",
  backgroundGradientToOpacity: 0.5,
  color: (opacity = 1) => `rgba(26, 255, 146, ${opacity})`,
  strokeWidth: 2, // optional, default 3
  barPercentage: 0.5,
  useShadowColorFromDataset: false // optional
};

const screenWidth = Dimensions.get("window").width;
const screenLength = Dimensions.get("window").height;

const data = [
  {
    name: "Instagram",
    time_spent: 2.2,
    color: "rgba(131, 167, 234, 1)",
    legendFontColor: "#7F7F7F",
    legendFontSize: 15
  },
  {
    name: "Tiktok",
    time_spent: 3.6,
    color: "#F00",
    legendFontColor: "#7F7F7F",
    legendFontSize: 15
  },
  {
    name: "Reddit",
    time_spent: 1.4,
    color: "red",
    legendFontColor: "#7F7F7F",
    legendFontSize: 15
  }
];

export default function HomeScreen() {
  return (
    <View>
      <PieChart
        data={data}
        width={screenWidth}
        height={200}
        chartConfig={chartConfig}
        accessor={"time_spent"}
        backgroundColor={"transparent"}
        paddingLeft={"15"}
        center={[100, 50]}
        absolute
      />
      <Text>QOTD</Text>
    </View>
  );
}
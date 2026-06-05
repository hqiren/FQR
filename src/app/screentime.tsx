import { View, Text, TouchableOpacity, StyleSheet, FlatList, TextInput } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { useState } from 'react';
import { useScreenTime } from '../context/ScreenTime';

export default function ScreenTimeScreen() {
  const { screenTimeData, addScreenTime, removeScreenTime } = useScreenTime();
  const [appName, setAppName] = useState('');
  const [time, setTime] = useState('');

  return (
    <SafeAreaView style={{ flex: 1 }}>

      {/* Back Button */}
      <TouchableOpacity 
        onPress={() => router.back()}
        style={{ padding: 16 }}
      >
        <Ionicons name='arrow-back-circle-outline' size={40} color='black' />
      </TouchableOpacity>

      <Text style={{ fontSize: 20, fontWeight: 'bold', textAlign: 'center' }}>
        Screen Time
      </Text>

      {/* Input Row */}
      <View style={{ flexDirection: 'row', padding: 16, gap: 8, alignItems: 'center' }}>
        <TextInput
          style={{ flex: 2, borderWidth: 1, borderRadius: 8, padding: 8, borderColor: '#ccc' }}
          placeholder="App name..."
          value={appName}
          onChangeText={setAppName}
        />
        <TextInput
          style={{ flex: 1, borderWidth: 1, borderRadius: 8, padding: 8, borderColor: '#ccc' }}
          placeholder="Hours..."
          value={time}
          onChangeText={setTime}
          keyboardType="numeric"
        />
        <TouchableOpacity onPress={() => {
          if (appName.trim() === '' || time.trim() === '') return;
          addScreenTime(Number(time), appName);
          setAppName('');
          setTime('');
        }}>
          <Ionicons name='add-circle' size={40} color='black' />
        </TouchableOpacity>
      </View>

      {/* List of entries */}
      <FlatList
        data={screenTimeData}
        keyExtractor={(item) => item.id.toString()}
        renderItem={({ item }) => (
          <View style={{ 
            flexDirection: 'row', 
            justifyContent: 'space-between',
            alignItems: 'center',
            backgroundColor: '#fff',
            padding: 16,
            borderRadius: 16,
            marginBottom: 10,
            marginHorizontal: 16
          }}>
            <Text>{item.app}: {item.time}h</Text>
            <TouchableOpacity onPress={() => removeScreenTime(item.id)}>
              <Ionicons name='trash-outline' size={24} color='black' />
            </TouchableOpacity>
          </View>
        )}
      />

    </SafeAreaView>
  );
}